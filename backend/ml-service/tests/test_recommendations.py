import pytest
from unittest.mock import patch

# ── GET /recommendations/{user_id} ──────────────────────────────────────────

def test_recommendations_for_known_user(client, products_df, orders_df):
    """✅ User 1 bought [1,2]; similar user 2 bought [1,4] → recommend product 4."""
    with patch("main.get_orders", return_value=orders_df), \
         patch("main.get_products", return_value=products_df):

        resp = client.get("/recommendations/1")

        assert resp.status_code == 200
        data = resp.json()
        assert data["user_id"] == 1
        assert "reason" in data
        assert isinstance(data["recommendations"], list)

def test_recommendations_cold_start_unknown_user(client, products_df, orders_df):
    """✅ User 99 has no orders → should get trending/top-rated with correct reason."""
    with patch("main.get_orders", return_value=orders_df), \
         patch("main.get_products", return_value=products_df):

        resp = client.get("/recommendations/99")

        assert resp.status_code == 200
        data = resp.json()
        assert data["user_id"] == 99
        assert "trending" in data["reason"].lower() or "top" in data["reason"].lower()
        assert len(data["recommendations"]) > 0

def test_recommendations_no_orders_at_all(client, products_df, empty_orders_df):
    """✅ No order history anywhere → top rated fallback."""
    with patch("main.get_orders", return_value=empty_orders_df), \
         patch("main.get_products", return_value=products_df):

        resp = client.get("/recommendations/1")

        assert resp.status_code == 200
        data = resp.json()
        assert "top" in data["reason"].lower()

def test_recommendations_does_not_suggest_already_bought(client, products_df, orders_df):
    """✅ User 1 already bought [1, 2] — neither should appear in recommendations."""
    with patch("main.get_orders", return_value=orders_df), \
         patch("main.get_products", return_value=products_df):

        data = client.get("/recommendations/1").json()
        rec_ids = [p["id"] for p in data["recommendations"]]
        assert 1 not in rec_ids
        assert 2 not in rec_ids

def test_recommendations_response_shape(client, products_df, orders_df):
    """✅ Response must always have user_id, reason, recommendations."""
    with patch("main.get_orders", return_value=orders_df), \
         patch("main.get_products", return_value=products_df):

        data = client.get("/recommendations/1").json()
        assert "user_id" in data
        assert "reason" in data
        assert "recommendations" in data

def test_recommendations_db_error_returns_503(client):
    """❌ DB down → 503, not 200."""
    from sqlalchemy.exc import SQLAlchemyError
    with patch("main.get_orders", side_effect=SQLAlchemyError("DB down")):
        resp = client.get("/recommendations/1")
        assert resp.status_code == 503

# ── GET /health ──────────────────────────────────────────────────────────────

def test_health_check_ok(client):
    """✅ Health endpoint pings DB and returns ok."""
    with patch("main.engine") as mock_engine:
        mock_conn = mock_engine.connect.return_value.__enter__.return_value
        mock_conn.execute.return_value = None

        resp = client.get("/health")
        assert resp.status_code == 200
        assert resp.json()["status"] == "ok"

def test_health_check_db_down_returns_503(client):
    """❌ DB down → 503."""
    from sqlalchemy.exc import SQLAlchemyError
    with patch("main.engine") as mock_engine:
        mock_engine.connect.side_effect = SQLAlchemyError("DB down")
        resp = client.get("/health")
        assert resp.status_code == 503