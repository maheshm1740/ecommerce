import pytest
import pandas as pd
from fastapi.testclient import TestClient
from unittest.mock import patch

# Import the FastAPI app
import sys, os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from main import app

@pytest.fixture
def client():
    return TestClient(app)

# ── reusable DataFrames ──────────────────────────────────────────────────────

@pytest.fixture
def products_df():
    return pd.DataFrame([
        {"id": 1, "name": "Laptop",  "category": "TECH",  "price": 999.0,  "stock": 10, "rating": 4.5, "reviewCount": 20},
        {"id": 2, "name": "Phone",   "category": "TECH",  "price": 499.0,  "stock": 5,  "rating": 4.2, "reviewCount": 15},
        {"id": 3, "name": "Shirt",   "category": "APPAREL","price": 29.0,  "stock": 50, "rating": 3.8, "reviewCount": 8},
        {"id": 4, "name": "Tablet",  "category": "TECH",  "price": 299.0,  "stock": 8,  "rating": 4.0, "reviewCount": 12},
        {"id": 5, "name": "Headset", "category": "TECH",  "price": 199.0,  "stock": 15, "rating": 4.3, "reviewCount": 10},
        {"id": 6, "name": "Jacket",  "category": "APPAREL","price": 89.0,  "stock": 30, "rating": 4.1, "reviewCount": 6},
    ])

@pytest.fixture
def orders_df():
    """Simulates 3 users with purchase history."""
    return pd.DataFrame([
        {"order_id": 1, "user_id": 1, "product_id": 1},
        {"order_id": 1, "user_id": 1, "product_id": 2},
        {"order_id": 2, "user_id": 2, "product_id": 1},
        {"order_id": 2, "user_id": 2, "product_id": 4},
        {"order_id": 3, "user_id": 3, "product_id": 2},
        {"order_id": 3, "user_id": 3, "product_id": 5},
    ])

@pytest.fixture
def empty_orders_df():
    return pd.DataFrame(columns=["order_id", "user_id", "product_id"])