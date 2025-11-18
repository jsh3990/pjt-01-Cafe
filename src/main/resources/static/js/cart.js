document.addEventListener('DOMContentLoaded', function() {

    // 1. 초기 설정
    let mainElement = document.querySelector('.shopping-cart-combined');
    let currentUserId = mainElement ? mainElement.getAttribute('data-member-id') : null;

    let fixedDeliveryFee = 2000;
    let isProcessingPayment = false;
    let API_BASE_URL = '/home/cart';

    // ==========================================
    // 2. 장바구니 API 통신 함수 (수량변경, 삭제)
    // ==========================================

    // 수량 변경 API 호출
    function changeQuantityCartItem(cartItemId, quantity) {
        return fetch(`${API_BASE_URL}/items/${cartItemId}?quantity=${quantity}`, {
            method: 'PATCH'
        }).then(response => response.text());
    }

    // 삭제 API 호출
    function deleteCartItem(cartItemId) {
        return fetch(`${API_BASE_URL}/items/${cartItemId}`, {
            method: 'DELETE'
        }).then(response => response.text());
    }

    // ==========================================
    // 3. 화면 업데이트 & 가격 계산 로직
    // ==========================================

    // 개별 아이템 가격 표시 업데이트
    function updateItemPriceDisplay(itemElement) {
        let basePrice = parseInt(itemElement.dataset.basePrice) || 0;
        let optionPrice = parseInt(itemElement.dataset.optionPrice) || 0;
        let quantity = parseInt(itemElement.querySelector('.item-quantity').dataset.quantity) || 0;

        let itemTotalPrice = (basePrice + optionPrice) * quantity;

        let priceDisplayElement = itemElement.querySelector('.item-price-display');
        if (priceDisplayElement) {
            priceDisplayElement.textContent = `${itemTotalPrice.toLocaleString('ko-KR')}원`;
        }
    }

    // 하단 주문 정보(총액, 배달비) 업데이트
    function updateOrderPrice(productTotal) {
        // 상품 금액
        let productPriceElement = document.getElementById('productPrice');
        if (productPriceElement) productPriceElement.textContent = `${productTotal.toLocaleString('ko-KR')}원`;

        let summaryTotalElement = document.getElementById('summaryTotalPrice');
        if (summaryTotalElement) summaryTotalElement.textContent = `${productTotal.toLocaleString('ko-KR')}원`;

        // 배달비 계산
        let currentDeliveryFee = 0;
        let deliveryButton = document.querySelector('.delivery-btn[data-type="delivery"]');
        if (deliveryButton && deliveryButton.classList.contains('active-delivery')) {
            currentDeliveryFee = fixedDeliveryFee;
        }

        let deliveryFeeElement = document.getElementById('deliveryFee');
        if (deliveryFeeElement) {
            deliveryFeeElement.textContent = currentDeliveryFee > 0 ? `${currentDeliveryFee.toLocaleString('ko-KR')}원` : '0원';
        }

        // 최종 결제 금액
        let finalTotal = productTotal + currentDeliveryFee;
        let finalTotalElement = document.getElementById('finalTotalPrice');
        if (finalTotalElement) finalTotalElement.textContent = `${finalTotal.toLocaleString('ko-KR')}원`;
    }

    // 전체 장바구니 합계 재계산
    function updateCartTotal() {
        let total = 0;
        let items = document.querySelectorAll('.cart-item');

        items.forEach(function(item) {
            // 체크된 항목만 합계에 포함
            let isChecked = item.querySelector('.item-checkbox-input').checked;
            if (isChecked) {
                let basePrice = parseInt(item.dataset.basePrice) || 0;
                let optionPrice = parseInt(item.dataset.optionPrice) || 0;
                let quantity = parseInt(item.querySelector('.item-quantity').dataset.quantity) || 0;
                total += (basePrice + optionPrice) * quantity;
            }
        });

        let formattedTotal = total.toLocaleString('ko-KR');
        let cartTotalElement = document.getElementById('totalCartPrice');
        if (cartTotalElement) cartTotalElement.textContent = `${formattedTotal}원`;

        updateOrderPrice(total);
        updatePaymentButtonState(); // 버튼 활성화 여부 체크
    }

    // ==========================================
    // 4. 이벤트 리스너 (클릭, 수량조절, 삭제)
    // ==========================================
    let cartContainer = document.querySelector('.item-list');
    if (cartContainer) {
        cartContainer.addEventListener('click', function(e) {
            let btn = e.target;
            let item = btn.closest('.cart-item');
            if (!item) return;

            // 수량 조절 (+ / -)
            if (btn.classList.contains('plus-btn') || btn.classList.contains('minus-btn')) {
                let quantitySpan = item.querySelector('.item-quantity');
                let currentQuantity = parseInt(quantitySpan.dataset.quantity);
                let newQuantity = currentQuantity;
                let cartItemId = item.dataset.cartItemId;

                if (btn.classList.contains('plus-btn')) {
                    newQuantity += 1;
                } else if (btn.classList.contains('minus-btn')) {
                    if (currentQuantity > 1) newQuantity -= 1;
                }

                if (newQuantity !== currentQuantity && cartItemId) {
                    changeQuantityCartItem(cartItemId, newQuantity).then(result => {
                        if (result === "change success") {
                            quantitySpan.dataset.quantity = newQuantity;
                            quantitySpan.textContent = newQuantity;
                            updateItemPriceDisplay(item);
                            updateCartTotal();
                        } else {
                            alert('수량 변경에 실패했습니다.');
                        }
                    });
                }
            }
            // 삭제 버튼 (x)
            else if (btn.classList.contains('item-remove')) {
                let cartItemId = item.dataset.cartItemId;
                if (cartItemId) {
                    if (!confirm('정말 삭제하시겠습니까?')) return;
                    deleteCartItem(cartItemId).then(result => {
                        if (result === "delete success") {
                            window.location.reload();
                        } else {
                            alert('삭제에 실패했습니다.');
                        }
                    });
                }
            }
            // 개별 체크박스
            else if (btn.classList.contains('item-checkbox-input')) {
                updateCartTotal();
            }
        });
    }

    // 전체 선택 체크박스
    let selectAllCheckbox = document.getElementById('selectAll');
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            let itemCheckboxes = document.querySelectorAll('.item-checkbox-input');
            itemCheckboxes.forEach(cb => cb.checked = selectAllCheckbox.checked);
            updateCartTotal();
        });
    }

    // 배달/포장 토글 버튼
    let deliveryToggle = document.querySelector('.delivery-toggle');
    if (deliveryToggle) {
        deliveryToggle.addEventListener('click', function(e) {
            if (e.target.classList.contains('delivery-btn')) {
                deliveryToggle.querySelectorAll('.delivery-btn').forEach(btn => btn.classList.remove('active-delivery'));
                e.target.classList.add('active-delivery');
                updateCartTotal();
            }
        });
    }

    // 선택 삭제 버튼
    let deleteSelectedBtn = document.getElementById('deleteSelectedBtn');
    if (deleteSelectedBtn) {
        deleteSelectedBtn.addEventListener('click', function() {
            let checkedItems = document.querySelectorAll('.cart-item .item-checkbox-input:checked');
            if (checkedItems.length === 0) {
                alert('삭제할 항목을 선택해주세요.');
                return;
            }

            if (!confirm(`선택된 ${checkedItems.length}개 항목을 삭제하시겠습니까?`)) return;

            let deletePromises = [];
            checkedItems.forEach(function(checkbox) {
                let cartItem = checkbox.closest('.cart-item');
                let cartItemId = cartItem.dataset.cartItemId;
                if (cartItemId) deletePromises.push(deleteCartItem(cartItemId));
            });

            Promise.all(deletePromises).then(() => window.location.reload());
        });
    }

    // ==========================================
    // 5. ⭐ [핵심] 주문 데이터 생성 (OrderVO 구조 맞춤)
    // ==========================================
    function preparePaymentData(selectedItems) {
        let orderItems = [];
        let totalQty = 0;

        selectedItems.forEach(function(checkbox) {
            let item = checkbox.closest('.cart-item');
            let qty = parseInt(item.querySelector('.item-quantity').dataset.quantity);

            // 1. 메뉴 ID 가져오기 (HTML에 th:data-menu-id가 있어야 함)
            let menuId = item.dataset.menuId || item.dataset.cartItemId;

            // 2. 옵션 정보 추출 (HTML 구조나 data 속성에서 가져옴)
            // ⚠️ 중요: 정확한 수집을 위해 HTML .cart-item 태그에 data-shot-count 등을 추가하는 것을 권장합니다.
            // 없으면 기본값 0으로 처리됩니다.
            let shot = parseInt(item.dataset.shotCount || 0);
            let vanillaSyrup = parseInt(item.dataset.vanillaSyrupCount || 0);
            let whippedCream = parseInt(item.dataset.whippedCreamCount || 0);

            // 온도 (ICE/HOT) 텍스트 파싱
            let tempText = item.querySelector('.item-temp') ? item.querySelector('.item-temp').textContent.trim() : 'ICE';

            // 텀블러 사용 여부 (옵션 텍스트에 '텀블러' 포함 여부로 판단)
            let optionsText = item.querySelector('.item-options') ? item.querySelector('.item-options').textContent : "";
            let isTumbler = optionsText.includes('텀블러') ? 1 : 0;

            // 리스트에 추가
            orderItems.push({
                menuId: menuId,
                menuItemName: item.querySelector('.item-name').textContent.trim(),
                quantity: qty,
                // 상세 옵션
                temp: tempText,
                tumbler: isTumbler,
                shot: shot,
                vanillaSyrup: vanillaSyrup,
                whippedCream: whippedCream
            });
            totalQty += qty;
        });

        // 3. 주문 유형 (배달/포장)
        let deliveryBtn = document.querySelector('.delivery-btn.active-delivery');
        let orderType = (deliveryBtn && deliveryBtn.dataset.type === 'delivery') ? "배달" : "포장";

        // 4. 총 결제 금액
        const totalStr = document.getElementById('finalTotalPrice').textContent;
        const finalPrice = parseInt(totalStr.replace(/[^0-9]/g, ''));

        // 5. ⭐ 매장 이름 가져오기 (cart.html의 hidden input)
        const storeNameInput = document.getElementById('currentStoreName');
        const storeName = storeNameInput ? storeNameInput.value : "";

        // 최종 데이터 반환 (OrderVO 구조)
        return {
            totalQuantity: totalQty,
            totalPrice: finalPrice,
            orderType: orderType,
            orderStatus: "주문접수",
            uId: currentUserId || "guest",
            storeName: storeName,  // ⭐ 매장 정보 전송
            orderItemList: orderItems // 상세 메뉴 리스트
        };
    }

    // ==========================================
    // 6. ⭐ [핵심] 결제 요청 (API 호출)
    // ==========================================
    async function handlePayment() {
        if (isProcessingPayment) return;

        let selectedItems = document.querySelectorAll('.cart-item .item-checkbox-input:checked');
        if (selectedItems.length === 0) {
            alert('결제할 상품을 선택해주세요.');
            return;
        }

        isProcessingPayment = true;
        setPaymentButtonLoading(true);

        // 데이터 준비
        let paymentData = preparePaymentData(selectedItems);

        // 디버깅용 로그 (나중에 삭제 가능)
        console.log("주문 전송 데이터:", paymentData);

        try {
            // API 호출 (/api/orders/create)
            const response = await fetch('/api/orders/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(paymentData)
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error('주문 실패: ' + errorText);
            }

            // 성공 시
            alert('주문이 완료되었습니다!');
            window.location.href = "/home/"; // 홈으로 이동

        } catch (error) {
            console.error('주문 처리 중 오류:', error);
            alert('주문 처리 중 오류가 발생했습니다.');
        } finally {
            setPaymentButtonLoading(false);
            isProcessingPayment = false;
        }
    }

    // 결제 버튼 UI 상태 관리
    function setPaymentButtonLoading(isLoading) {
        let btn = document.querySelector('.payment-btn');
        if (btn) {
            if (isLoading) {
                btn.disabled = true;
                btn.textContent = '주문 처리 중...';
            } else {
                btn.disabled = false;
                btn.textContent = '결제하기';
            }
        }
    }

    function updatePaymentButtonState() {
        let btn = document.querySelector('.payment-btn');
        let count = document.querySelectorAll('.cart-item .item-checkbox-input:checked').length;
        if (btn) {
            btn.disabled = count === 0;
            btn.style.opacity = count === 0 ? '0.6' : '1';
        }
    }

    // 결제 버튼 이벤트 연결
    let paymentButton = document.querySelector('.payment-btn');
    if (paymentButton) {
        paymentButton.addEventListener('click', handlePayment);
    }

    // 페이지 로드 시 초기 계산 실행
    updateCartTotal();
});