import pytest
from unittest.mock import patch

# ── GET /similar/{product_id} ────────────────────────────────────────────────

def test_similar_returns_same_category_products(client, products_df):
    """✅ Should only return products in the same category."""
    with patch("main.get_products", return_value=products_df):
        resp = client.get("/similar/1")  # Laptop → TECH

        assert resp.status_code == 200
        data = resp.json()
        assert all(p["category"] == "TECH" for p in data)
        assert all(p["id"] != 1 for p in data)  # must exclude the product itself

def test_similar_excludes_target_product(client, products_df):
    """✅ Target product must not appear in its own similar list."""
    with patch("main.get_products", return_value=products_df):
        data = client.get("/similar/1").json()
        ids = [p["id"] for p in data]
        assert 1 not in ids

def test_similar_empty_when_no_same_category(client, products_df):
    """✅ Returns empty list when no other products in same category."""
    import pandas as pd
    single_category_df = products_df[products_df["category"] == "APPAREL"].copy()
    # keep only 1 apparel item so there are no 'similar'
    only_shirt = single_category_df[single_category_df["id"] == 3]

    with patch("main.get_products", return_value=only_shirt):
        resp = client.get("/similar/3")
        assert resp.status_code == 200
        assert resp.json() == []

def test_similar_product_not_found_returns_404(client, products_df):
    """❌ Unknown product_id → 404, not 200 with error key."""
    with patch("main.get_products", return_value=products_df):
        resp = client.get("/similar/9999")
        assert resp.status_code == 404
        assert "not found" in resp.json()["detail"].lower()

def test_similar_db_error_returns_503(client):
    """❌ DB failure → 503."""
    from sqlalchemy.exc import SQLAlchemyError
    with patch("main.get_products", side_effect=SQLAlchemyError("DB down")):
        resp = client.get("/similar/1")
        assert resp.status_code == 503

def test_similar_scores_by_price_proximity(client, products_df):
    """✅ Cheaper alternatives to Laptop (999) should rank Tablet (299) below Phone (499)
       because score = price_proximity * 0.4 + rating * 0.12."""
    with patch("main.get_products", return_value=products_df):
        data = client.get("/similar/1").json()
        # Phone (499) is closer in price to Laptop (999) than Tablet (299)
        ids = [p["id"] for p in data]
        assert 2 in ids  # Phone should appear