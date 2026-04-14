/*-
 * #%L
 * Chat Assistant Add-on
 * %%
 * Copyright (C) 2023 - 2026 Flowing Code
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
window.fcChatAssistantMovement = (root, item, container, fab, marginRaw, sensitivityRaw) => {
    // Prevent duplicate initialization
    const guard = `__fcChatAssistantMovement`;
    if (item[guard]) {
        return;
    }
    item[guard] = true;
    const margin = parseFloat(marginRaw);
    const sensitivity = parseFloat(sensitivityRaw);
    const sizeTransition = 'transform 0.2s ease';
    const snapTransition = 'all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
    const position = { x: margin, y: margin };
    const initialPosition = { x: margin, y: margin };

    let screenWidth = window.innerWidth;
    let screenHeight = window.innerHeight;
    let isDragging = false;

    item.style.transition = sizeTransition;

    window.addEventListener("resize", (_) => {
        screenWidth = window.innerWidth;
        screenHeight = window.innerHeight;
        
        // Adjust container dimensions to fit within screen bounds
        if (container) {
            const rect = container.getBoundingClientRect();
            let widthAdjustment = 0;
            let heightAdjustment = 0;
            if (rect.left < 0) {
                widthAdjustment = Math.abs(rect.left);
            }
            if (rect.right > screenWidth) {
                widthAdjustment = Math.max(widthAdjustment, rect.right - screenWidth);
            }
            if (rect.top < 0) {
                heightAdjustment = Math.abs(rect.top);
            }
            if (rect.bottom > screenHeight) {
                heightAdjustment = Math.max(heightAdjustment, rect.bottom - screenHeight);
            }
            // Apply adjustments
            if (widthAdjustment > 0) {
                const minWidth = parseFloat(container.style.minWidth) || 0;
                const newWidth = Math.max(minWidth, rect.width - widthAdjustment);
                container.style.width = newWidth + 'px';
            }
            if (heightAdjustment > 0) {
                const minHeight = parseFloat(container.style.minHeight) || 0;
                const newHeight = Math.max(minHeight, rect.height - heightAdjustment);
                container.style.height = newHeight + 'px';
            }
        }
        
        // Reposition the item to ensure it stays within the new screen bounds
        snapToBoundary();
    });

    // Update FAB position
    function updatePosition() {
        item.style.right = position.x + 'px';
        item.style.bottom = position.y + 'px';
    }

    // Ensure the item stays within the screen and margin bounds
    function snapToBoundary() {
        // Get current dimensions to account for transforms
        const itemRect = fab.getBoundingClientRect();
        
        const xMax = Math.max(margin, screenWidth - itemRect.width - margin);
        const yMax = Math.max(margin, screenHeight - itemRect.height - margin);
        const x = position.x;
        const y = position.y;
        if (x < margin) position.x = margin;
        if (x > xMax) position.x = xMax;
        if (y < margin) position.y = margin;
        if (y > yMax) position.y = yMax;
        updatePosition();
    }

    // Determine if the pointer event should be treated as a click (no significant movement, based on sensitivity threshold)
    function isClickOnlyEvent() {
        const dx = Math.abs(position.x - initialPosition.x);
        const dy = Math.abs(position.y - initialPosition.y);
        return dx < sensitivity && dy < sensitivity;
    }

    item.addEventListener('pointerdown', (e) => {
        isDragging = true;
        fab.classList.add('dragging');
        item.setPointerCapture(e.pointerId);
        item.style.transition = sizeTransition;
        initialPosition.x = position.x;
        initialPosition.y = position.y;
    });

    item.addEventListener('pointermove', (e) => {
        if (!isDragging) return;
        const itemRect = fab.getBoundingClientRect();
        // Calculate position from right and bottom edges
        position.x = screenWidth - e.clientX - (itemRect.width / 2);
        position.y = screenHeight - e.clientY - (itemRect.height / 2);
        
        updatePosition();
    });

    item.addEventListener('pointerup', (e) => stopDragging(e));
    item.addEventListener('pointerleave', (e) => stopDragging(e));
    item.addEventListener('pointercancel', (e) => stopDragging(e));

    function stopDragging(e) {
        if(isDragging) {
            isDragging = false;
            item.style.transition = snapTransition + ', ' + sizeTransition;
            fab.classList.remove('dragging');
            item.releasePointerCapture(e.pointerId);
            snapToBoundary();
            if (isClickOnlyEvent()) {
                root.$server?.onClick();
            }
        }
    }

    updatePosition();
};
