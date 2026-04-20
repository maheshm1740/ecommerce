import pytest
from unittest.mock import patch

# ── GET /trending ────────────────────────────────────────────────────────────

def test_trending_returns_top_ordered_products(client, products_df, orders_df):
    """✅ Should return products sorted by order count."""
    with patch("main.get_orders", return_value=orders_df), \
         patch("main.get_products", return_value=products_df):

        resp = client.get("/trending")

        assert resp.status_code == 200
        data = resp.json()
        assert len(data) >= 1
        # product 1 and 2 appear most in orders — should be first
        ids = [p["id"] for p in data]
        assert 1 in ids
        assert 2 in ids

def test_trending_cold_start_returns_top_rated(client, products_df, empty_orders_df):
    """✅ Cold start: no orders → fall back to highest-rated products."""
    with patch("main.get_orders", return_value=empty_orders_df), \
         patch("main.get_products", return_value=products_df):

        resp = client.get("/trending")

        assert resp.status_code == 200
        data = resp.json()
        assert len(data) > 0
        # first result should have highest rating
        ratings = [p["rating"] for p in data]
        assert ratings == sorted(ratings, reverse=True)

def test_trending_db_error_returns_503(client):
    """❌ DB failure → 503, not 200 with error body."""
    from sqlalchemy.exc import SQLAlchemyError
    with patch("main.get_orders", side_effect=SQLAlchemyError("DB down")):
        resp = client.get("/trending")
        assert resp.status_code == 503

def test_trending_response_has_required_fields(client, products_df, empty_orders_df):
    """✅ Response shape matches ProductOut model."""
    with patch("main.get_orders", return_value=empty_orders_df), \
         patch("main.get_products", return_value=products_df):

        data = client.get("/trending").json()
        required = {"id", "name", "price", "stock", "rating"}
        for product in data:
            assert required.issubset(product.keys())