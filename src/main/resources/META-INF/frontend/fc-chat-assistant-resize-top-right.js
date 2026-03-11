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
window.fcChatAssistantResizeTopRight = (item, container, popoverTag, sizeRaw, maxSizeRaw) => {
    const size = parseFloat(sizeRaw);
    const maxSize = parseFloat(maxSizeRaw);
    const overlayTag = "vaadin-popover-overlay".toUpperCase();

    let minWidth = 0;
    let minHeight = 0;
    let maxWidth = Infinity;
    let maxHeight = Infinity;
    let overlay;
    let isDragging = false;

    window.requestAnimationFrame(fetchOverlay);
    setTimeout(fetchOverlay, 2000); // in case the overlay is not available immediately, check again after 2 seconds

    // Fetch the root overlay component
    function fetchOverlay() {
        if (!overlay) {
            overlay = [...document.getElementsByClassName(popoverTag)].find(p => p.tagName == overlayTag);
        }
    }

    // Restrict dragging capability to when the popover content has enough space in the corresponding directions
    function shouldDrag() {
        const topRule = overlay && overlay.style.bottom && !overlay.style.top;
        const rightRule = overlay && overlay.style.left && !overlay.style.right;
        return topRule && rightRule;
    }

    item.addEventListener('pointerenter', (e) => {
        if (shouldDrag()) {
            item.classList.add('active');
            item.setPointerCapture(e.pointerId);
            minHeight = container.style.minHeight ? parseFloat(container.style.minHeight) : 0;
            minWidth = container.style.minWidth ? parseFloat(container.style.minWidth) : 0;
            maxWidth = container.style.maxWidth ? parseFloat(container.style.maxWidth) : Infinity;
            maxHeight = container.style.maxHeight ? parseFloat(container.style.maxHeight) : Infinity;
        }
        else {
            item.classList.remove('active');
        }
    });

    item.addEventListener('pointerdown', (_) => {
        isDragging = shouldDrag();
        if (isDragging) {
            item.style.height = maxSize + 'px';
            item.style.width = maxSize + 'px';
            item.style.marginTop = -(maxSize / 2) + 'px';
            item.style.marginRight = -(maxSize / 2) + 'px';
        }
    });

    item.addEventListener('pointermove', (e) => {
        if (!isDragging) return;
        const offsetY = container.getBoundingClientRect().top - e.clientY;
        const newHeight = offsetY + container.clientHeight;
        if(newHeight >= minHeight && newHeight <= maxHeight) {
            container.style.height = newHeight + 'px';
        }
        const offsetX = e.clientX - container.getBoundingClientRect().right;
        const newWidth = offsetX + container.clientWidth;
        if (newWidth >= minWidth && newWidth <= maxWidth) {
            container.style.width = newWidth + 'px';
        }
    });

    item.addEventListener('pointerup', (e) => {
        isDragging = false;
        item.style.height = size + 'px';
        item.style.width = size + 'px';
        item.style.marginTop = '';
        item.style.marginRight = '';
        if (item.hasPointerCapture(e.pointerId)) {
            item.releasePointerCapture(e.pointerId);
        }
    });

    item.addEventListener('pointerleave', (e) => {
        isDragging = false;
        item.style.height = size + 'px';
        item.style.width = size + 'px';
        item.style.marginTop = '';
        item.style.marginRight = '';
        if (item.hasPointerCapture(e.pointerId)) {
            item.releasePointerCapture(e.pointerId);
        }
    });
};
