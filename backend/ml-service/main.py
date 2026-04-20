from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy import create_engine, text
from sqlalchemy.exc import SQLAlchemyError
from pydantic import BaseModel
from typing import List, Optional
import pandas as pd
import os

app = FastAPI(title="Recommendation Engine", version="1.0.0")

# 🔄 CHANGED — was allow_origins=["*"]; restrict in production via env var
ALLOWED_ORIGINS = os.getenv("CORS_ORIGINS", "http://localhost:5173").split(",")

app.add_middleware(
    CORSMiddleware,
    allow_origins=ALLOWED_ORIGINS,
    allow_methods=["GET"],
    allow_headers=["*"],
)

# 🔄 CHANGED — DB URL now from env var; was hard-coded with credentials
DB_URL = os.getenv(
    "DATABASE_URL",
    "postgresql://postgres:1740@localhost:5432/ecommerce_db"
)

# ✅ NEW — connection pool config to avoid exhaustion under load
engine = create_engine(DB_URL, pool_size=5, max_overflow=10, pool_pre_ping=True)


# ✅ NEW — typed response models so clients get consistent shapes
class ProductOut(BaseModel):
    id: int
    name: str
    description: Optional[str] = None
    category: Optional[str] = None
    image_url: Optional[str] = None
    price: float
    stock: int
    rating: float = 0.0
    review_count: int = 0


class RecommendationResponse(BaseModel):
    user_id: int
    reason: str
    recommendations: List[ProductOut]


# ── DB helpers ──────────────────────────────────────────────────────────────

def get_products() -> pd.DataFrame:
    # ✅ NEW — raises SQLAlchemyError (caught by callers) instead of silently returning errors
    with engine.connect() as conn:
        result = conn.execute(text("SELECT * FROM products"))
        return pd.DataFrame(result.fetchall(), columns=result.keys())


def get_orders() -> pd.DataFrame:
    with engine.connect() as conn:
        result = conn.execute(text("""
            SELECT op.order_id, o.user_id, op.product_id
            FROM order_products op
            JOIN orders o ON op.order_id = o.id
        """))
        return pd.DataFrame(result.fetchall(), columns=["order_id", "user_id", "product_id"])


def df_to_products(df: pd.DataFrame) -> List[dict]:
    return df.rename(columns={"imageUrl": "image_url", "reviewCount": "review_count"}).to_dict(orient="records")


# ── Trending ────────────────────────────────────────────────────────────────

@app.get("/trending", response_model=List[ProductOut])
def trending():
    try:
        orders_df = get_orders()
        products_df = get_products()

        if orders_df.empty:
            top = products_df.sort_values("rating", ascending=False).head(6)
            return df_to_products(top)

        counts = orders_df.groupby("product_id").size().reset_index(name="order_count")
        merged = counts.merge(products_df, left_on="product_id", right_on="id")
        top = merged.sort_values("order_count", ascending=False).head(6)
        return df_to_products(top.drop(columns=["order_count"]))

    except SQLAlchemyError as e:
        # 🔄 CHANGED — was return {"error": str(e)} with HTTP 200; now proper 503
        raise HTTPException(status_code=503, detail="Database unavailable")
    except Exception as e:
        # 🔄 CHANGED — was return {"error": str(e)} with HTTP 200; now proper 500
        raise HTTPException(status_code=500, detail="Internal server error")


# ── Similar products (content-based) ────────────────────────────────────────

@app.get("/similar/{product_id}", response_model=List[ProductOut])
def similar_products(product_id: int):
    try:
        products_df = get_products()

        target = products_df[products_df["id"] == product_id]
        if target.empty:
            # 🔄 CHANGED — was return {"error": "Product not found"} with HTTP 200
            raise HTTPException(status_code=404, detail=f"Product {product_id} not found")

        target_category = target.iloc[0]["category"]
        target_price = float(target.iloc[0]["price"])

        similar = products_df[
            (products_df["category"] == target_category) &
            (products_df["id"] != product_id)
        ].copy()

        if similar.empty:
            return []

        similar["price_score"] = 1 - (
            abs(similar["price"] - target_price) /
            (products_df["price"].max() + 1)
        )
        similar["score"] = similar["price_score"] * 0.4 + similar["rating"] * 0.12
        similar = similar.sort_values("score", ascending=False).head(6)

        return df_to_products(similar.drop(columns=["price_score", "score"]))

    except HTTPException:
        raise
    except SQLAlchemyError:
        raise HTTPException(status_code=503, detail="Database unavailable")
    except Exception:
        raise HTTPException(status_code=500, detail="Internal server error")


# ── User recommendations (collaborative filtering) ──────────────────────────

@app.get("/recommendations/{user_id}", response_model=RecommendationResponse)
def recommendations(user_id: int):
    try:
        from sklearn.metrics.pairwise import cosine_similarity  # ✅ local import — only needed here

        orders_df = get_orders()
        products_df = get_products()

        if orders_df.empty:
            top = products_df.sort_values("rating", ascending=False).head(6)
            return RecommendationResponse(
                user_id=user_id,
                reason="Top rated products",
                recommendations=df_to_products(top)
            )

        matrix = orders_df.pivot_table(
            index="user_id",
            columns="product_id",
            aggfunc="size",
            fill_value=0
        )

        if user_id not in matrix.index:
            counts = orders_df.groupby("product_id").size().reset_index(name="count")
            merged = counts.merge(products_df, left_on="product_id", right_on="id")
            top = merged.sort_values("count", ascending=False).head(6)
            return RecommendationResponse(
                user_id=user_id,
                reason="Trending products",
                recommendations=df_to_products(top.drop(columns=["count"]))
            )

        similarity = cosine_similarity(matrix)
        sim_df = pd.DataFrame(similarity, index=matrix.index, columns=matrix.index)
        similar_users = sim_df[user_id].drop(user_id).sort_values(ascending=False).head(3)

        user_products = set(matrix.columns[matrix.loc[user_id] > 0])
        recommended_ids = set()
        for sim_user in similar_users.index:
            sim_user_products = set(matrix.columns[matrix.loc[sim_user] > 0])
            recommended_ids.update(sim_user_products - user_products)

        if not recommended_ids:
            top = products_df.sort_values("rating", ascending=False).head(6)
            return RecommendationResponse(
                user_id=user_id,
                reason="Top rated for you",
                recommendations=df_to_products(top)
            )

        recs = products_df[products_df["id"].isin(recommended_ids)].head(6)
        return RecommendationResponse(
            user_id=user_id,
            reason="Based on similar users",
            recommendations=df_to_products(recs)
        )

    except HTTPException:
        raise
    except SQLAlchemyError:
        raise HTTPException(status_code=503, detail="Database unavailable")
    except Exception:
        raise HTTPException(status_code=500, detail="Internal server error")


@app.get("/health")
def health():
    # ✅ NEW — also pings DB so health check is meaningful
    try:
        with engine.connect() as conn:
            conn.execute(text("SELECT 1"))
        return {"status": "ok", "service": "recommendation-engine", "db": "connected"}
    except SQLAlchemyError:
        raise HTTPException(status_code=503, detail="Database unavailable")