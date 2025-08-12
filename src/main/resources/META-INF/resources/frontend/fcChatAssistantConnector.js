/*-
 * #%L
 * Chat Assistant Add-on
 * %%
 * Copyright (C) 2025 Flowing Code
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
(function () {
    window.Vaadin.Flow.fcChatAssistantConnector = {
        observePopoverResize: (popover) => {
            // Skip the following logic on mobile devices by checking viewport width.
            if (window.innerWidth <= 768) {
                return;
            }

            if (popover.$connector) {
                return;
            }

            popover.$connector = {};

            // Find the resizable container inside the popover
            const resizableContainer = popover.querySelector('.chat-assistant-resizable-vertical-layout');
            if (!resizableContainer) return;

            popover.addEventListener('opened-changed', e => {
                if (e.detail.value) {
                    const popoverOverlay = resizableContainer.parentElement;
                    const overlay = popoverOverlay.shadowRoot?.querySelector('[part="overlay"]');
                    // Track overlay position changes and keep container inside viewport
                    trackOverlayPosition(overlay, resizableContainer, () => clampToViewport(resizableContainer));
                }
            });

            // On drag/resize start (mouse), reset size restrictions so user can freely resize
            resizableContainer.addEventListener("mousedown", e => {
                resizableContainer.style.maxHeight = '';
                resizableContainer.style.maxWidth = '';
            });
            // On drag/resize start (touch), reset size restrictions so user can freely resize
            resizableContainer.addEventListener("touchstart", e => {
                resizableContainer.style.maxHeight = '';
                resizableContainer.style.maxWidth = '';
            });

            // Debounce calls to avoid excessive recalculations on rapid resize
            const debouncedClamp = debounce(() => clampToViewport(resizableContainer));

            new ResizeObserver(() => {
                const popoverOverlay = resizableContainer.parentElement;
                const overlay = popoverOverlay.shadowRoot?.querySelector('[part="overlay"]');
                if (!overlay) return;

                debouncedClamp();
            }).observe(resizableContainer);


            function debounce(callback) {
                let rafId;
                return () => {
                    cancelAnimationFrame(rafId);
                    rafId = requestAnimationFrame(callback);
                };
            }

            /**
             * Restricts the size and position of a resizable container so that it remains fully visible
             * within the browser's viewport, applying a small padding to keep it from touching the edges.
             *
             * This function calculates how much space is available on each side of the container
             * (top, bottom, left, right) relative to the viewport. If the container would overflow
             * on a given side, it adjusts `maxWidth`/`maxHeight` and aligns it to the opposite side
             * with a fixed padding.
             *
             * - If there isn't enough space on the right, it clamps width and aligns to the left.
             * - If there isn't enough space on the left, it clamps width and aligns to the right.
             * - If there isn't enough space at the bottom, it clamps height and aligns to the top.
             * - If there isn't enough space at the top, it clamps height and aligns to the bottom.
             *
             * @param {HTMLElement} resizableContainer - The element whose size and position should be clamped to the viewport.
             */
            function clampToViewport(resizableContainer) {
                const boundingClientRect = resizableContainer.getBoundingClientRect();

                const containerWidthRight = boundingClientRect.width + (window.innerWidth - boundingClientRect.right);
                const containerWidthLeft = boundingClientRect.left + boundingClientRect.width;
                const containerHeightBottom = boundingClientRect.height + (window.innerHeight - boundingClientRect.bottom);
                const containerHeightTop = boundingClientRect.top + boundingClientRect.height;

                const padding = 5;
                const paddingPx = padding + "px";

                if (containerWidthRight >= window.innerWidth) {
                    resizableContainer.style.maxWidth = (boundingClientRect.right - padding) + "px";
                    resizableContainer.style.left = paddingPx;
                } else if (containerWidthLeft >= window.innerWidth) {
                    resizableContainer.style.maxWidth = (window.innerWidth - boundingClientRect.left - padding) + "px";
                    resizableContainer.style.right = paddingPx;
                }

                if (containerHeightBottom >= window.innerHeight) {
                    resizableContainer.style.maxHeight = (boundingClientRect.bottom - padding) + "px";
                    resizableContainer.style.top = paddingPx;
                } else if (containerHeightTop >= window.innerHeight) {
                    resizableContainer.style.maxHeight = (window.innerHeight - boundingClientRect.top - padding) + "px";
                    resizableContainer.style.bottom = paddingPx;
                }
            }

            /**
             * Continuously tracks the position of an overlay element and triggers a callback
             * when the overlay's position has stabilized (i.e., changes are within the given buffer).
             *
             * This function uses `requestAnimationFrame` to check the overlay's position every frame.
             * If the overlay moves more than `positionBuffer` pixels horizontally or vertically,
             * tracking continues without calling the callback.
             * Once the position changes are smaller than `positionBuffer`, the callback is invoked.
             *
             * @param {HTMLElement} overlay - The overlay element to track. Must support `.checkVisibility()`.
             * @param {HTMLElement} resizableContainer - The container related to the overlay (not used directly here,
             *     but often used by the callback to adjust size).
             * @param {Function} callback - Function to call when the overlay position is stable.
             * @param {number} [positionBuffer=10] - The minimum pixel movement threshold before considering the overlay stable.
             */
            function trackOverlayPosition(overlay, resizableContainer, callback, positionBuffer = 10) {
                let lastTop = 0;
                let lastLeft = 0;
                let frameId;

                function checkPosition() {
                    if (!isVisible(overlay)) {
                        cancelAnimationFrame(frameId);
                        return;
                    }

                    const rect = overlay.getBoundingClientRect();
                    const deltaTop = Math.abs(rect.top - lastTop);
                    const deltaLeft = Math.abs(rect.left - lastLeft);
                    if (deltaTop > positionBuffer || deltaLeft > positionBuffer) {
                        lastTop = rect.top;
                        lastLeft = rect.left;
                    } else {
                        callback();
                    }

                    frameId = requestAnimationFrame(checkPosition);
                }

                frameId = requestAnimationFrame(checkPosition);
            }

            function isVisible(el) {
                if (!el) return false;

                if (typeof el.checkVisibility === 'function') {
                    // Use native checkVisibility if available
                    return el.checkVisibility();
                }

                // Fallback: check CSS display and visibility
                const style = getComputedStyle(el);
                return style.display !== 'none' && style.visibility !== 'hidden';
            }
        },
    }
})();
