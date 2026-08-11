async function apiRequest(url, method, body) {
    const options = {
        method: method,
        headers: { 'Content-Type': 'application/json' }
    };
    if (body !== undefined && body !== null) {
        options.body = JSON.stringify(body);
    }
    try {
        const res = await fetch(url, options);
        if (res.status === 401 || res.status === 403) {
            window.location.href = '/login';
            return null;
        }
        const data = await res.json().catch(() => ({}));
        if (!res.ok) {
            showToast(data.message || 'Something went wrong', 'error');
            return null;
        }
        return data;
    } catch (err) {
        showToast('Network error. Please try again.', 'error');
        return null;
    }
}

async function addToCart(productId, quantity) {
    const result = await apiRequest('/api/cart', 'POST', { productId: productId, quantity: quantity || 1 });
    if (result) {
        showToast(result.message || 'Added to cart', 'success');
        refreshCartCount();
    }
}

function addToCartFromCard(button) {
    const productId = button.getAttribute('data-product-id');
    addToCart(Number(productId), 1);
}

function addToCartFromDetails(button) {
    const productId = button.getAttribute('data-product-id');
    const stock = Number(button.getAttribute('data-stock'));
    let qty = document.getElementById('qtyInput').value;
    qty = Math.max(1, Math.min(Number(qty), stock));
    addToCart(Number(productId), qty);
}

function changeQty(delta) {
    const input = document.getElementById('qtyInput');
    const max = Number(input.max) || 999;
    let value = Number(input.value) + delta;
    if (value < 1) value = 1;
    if (value > max) value = max;
    input.value = value;
}

async function updateCartItem(cartItemId, quantity) {
    if (quantity < 1) return;
    const result = await apiRequest('/api/cart/' + cartItemId, 'PUT', { quantity: quantity });
    if (result) {
        location.reload();
    }
}

async function removeCartItem(cartItemId) {
    const result = await apiRequest('/api/cart/' + cartItemId, 'DELETE');
    if (result) {
        location.reload();
    }
}

async function refreshCartCount() {
    const data = await apiRequest('/api/cart', 'GET');
    if (data) {
        const badge = document.querySelector('.badge');
        if (badge) {
            badge.textContent = data.totalItems;
        }
    }
}
