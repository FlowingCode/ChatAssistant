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
window.fcChatAssistantResizeBottom = (item, container, popoverTag, sizeRaw, maxSizeRaw) => {
    const size = parseFloat(sizeRaw);
    const maxSize = parseFloat(maxSizeRaw);
    const overlayTag = "vaadin-popover-overlay".toUpperCase();

    let minHeight = 0;
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

    // Restrict dragging capability to when the popover content has enough space in the corresponding direction
    function shouldDrag() {
        return overlay && !overlay.style.bottom && overlay.style.top;
    }

    item.addEventListener('pointerenter', (e) => {
        if (shouldDrag()) {
            item.classList.add('active');
            item.setPointerCapture(e.pointerId);
            minHeight = container.style.minHeight ? parseFloat(container.style.minHeight) : 0;
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
            item.style.marginBottom = -(maxSize / 2) + 'px';
        }
    });

    item.addEventListener('pointermove', (e) => {
        if (!isDragging) return;
        const offsetY = e.clientY - container.getBoundingClientRect().bottom;
        const newHeight = offsetY + container.clientHeight;
        if (newHeight >= minHeight && newHeight <= maxHeight) {
            container.style.height = newHeight + 'px';
        }
    });

    item.addEventListener('pointerup', (e) => {
        isDragging = false;
        item.style.height = size + 'px';
        item.style.marginBottom = '';
        if (item.hasPointerCapture(e.pointerId)) {
            item.releasePointerCapture(e.pointerId);
        }
    });

    item.addEventListener('pointerleave', (e) => {
        isDragging = false;
        item.style.height = size + 'px';
        item.style.marginBottom = '';
        if (item.hasPointerCapture(e.pointerId)) {
            item.releasePointerCapture(e.pointerId);
        }
    });
};
