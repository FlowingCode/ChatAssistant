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
// The root element (<animated-fab>) has no backing web component, so register a minimal custom
// element whose disconnectedCallback runs the teardown callbacks that the movement and resize
// modules push onto root.__fcCleanups. Without an upgraded element the browser never fires
// disconnectedCallback, leaving the window/observer listeners uncleaned and the init guards set.
if (!customElements.get('animated-fab')) {
    customElements.define('animated-fab', class extends HTMLElement {
        disconnectedCallback() {
            (this.__fcCleanups || []).forEach(fn => {
                try {
                    fn();
                } catch (e) {
                    // keep running the remaining teardown callbacks
                }
            });
            this.__fcCleanups = [];
        }
    });
}

// Lifts the FAB wrapper to <body> while it is anchored to the viewport, so its position:fixed
// resolves against the viewport rather than an ancestor containing block. Any ancestor with
// transform/filter/backdrop-filter/perspective/contain/will-change (e.g. Aura's AppLayout navbar)
// would otherwise trap the wrapper. When it is not anchored, the wrapper is returned to its home
// slot so position:absolute stays relative to its container. Idempotent.
window.fcChatAssistantPortalFab = (item, anchored) => {
    if (anchored) {
        if (item.parentNode !== document.body) {
            // Remember where the wrapper lived so it can be put back on teardown / un-anchor.
            item.__fcHome = item.__fcHome || { parent: item.parentNode, next: item.nextSibling };
            document.body.appendChild(item);
        }
    } else if (item.parentNode === document.body) {
        fcChatAssistantRestoreFab(item);
    }
};

// Returns the wrapper to its recorded home slot (falling back to appendChild if the recorded next
// sibling is gone), so a detached host takes the wrapper with it instead of leaking it in <body>.
function fcChatAssistantRestoreFab(item) {
    const home = item.__fcHome;
    if (home && home.parent) {
        if (home.next && home.next.parentNode === home.parent) {
            home.parent.insertBefore(item, home.next);
        } else {
            home.parent.appendChild(item);
        }
    }
    item.__fcHome = null;
}

// Resolves the FAB's rendered size, falling back to the offset/CSS size when the element has not
// been laid out yet (getBoundingClientRect returns 0 before the first layout pass).
function fcChatAssistantSize(fab) {
    const rect = fab.getBoundingClientRect();
    const width = rect.width || fab.offsetWidth || parseFloat(fab.style.width) || 0;
    const height = rect.height || fab.offsetHeight || parseFloat(fab.style.height) || 0;
    return { width, height };
}

// Returns the box the FAB is positioned against: the viewport when fixed, otherwise its offset
// parent (e.g. a containing div). The right/bottom offsets are relative to this box.
function fcChatAssistantBounds(item) {
    if (getComputedStyle(item).position === 'fixed' || !item.offsetParent) {
        return { width: window.innerWidth, height: window.innerHeight };
    }
    const rect = item.offsetParent.getBoundingClientRect();
    return { width: rect.width, height: rect.height };
}

// Computes the FAB's position, expressed as right/bottom offsets, for the given corner.
function fcChatAssistantCornerPosition(item, fab, corner, margin) {
    const size = fcChatAssistantSize(fab);
    const bounds = fcChatAssistantBounds(item);
    const right = margin;
    const bottom = margin;
    const left = Math.max(margin, bounds.width - size.width - margin);
    const top = Math.max(margin, bounds.height - size.height - margin);
    switch (corner) {
        case 'BOTTOM_LEFT': return { x: left, y: bottom };
        case 'TOP_RIGHT': return { x: right, y: top };
        case 'TOP_LEFT': return { x: left, y: top };
        case 'BOTTOM_RIGHT':
        default: return { x: right, y: bottom };
    }
}

window.fcChatAssistantMovement = (root, item, fab, marginRaw, sensitivityRaw, positionRaw) => {
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
    // Expose the live position so the reset hook can move the FAB after initialization.
    item.__fcPosition = position;

    let screenWidth = window.innerWidth;
    let screenHeight = window.innerHeight;
    let isDragging = false;

    item.style.transition = sizeTransition;

    const resizeHandler = (_) => {
        screenWidth = window.innerWidth;
        screenHeight = window.innerHeight;

        // The popover content part clamps itself to the viewport (max-height/width: 100%), so no
        // manual container shrinking is needed here. Just keep the FAB within the new screen bounds.
        snapToBoundary();
    };
    window.addEventListener("resize", resizeHandler);

    (root.__fcCleanups = root.__fcCleanups || []).push(() => {
        window.removeEventListener("resize", resizeHandler);
        window.fcChatAssistantMobileModeOff?.(root);
        window.fcChatAssistantScreenSizeOffAll?.(root);
        // Put the wrapper back in its home slot so it is removed with the detached host, not left
        // behind in <body>; on reattach this function re-runs and re-portals it.
        fcChatAssistantRestoreFab(item);
        // Clear the init guards so movement re-initializes on reattach.
        item[guard] = false;
        root['fc-chat-assistant-drag-listener'] = null;
    });

    // Escape any ancestor containing block by portaling the wrapper to <body> while anchored.
    window.fcChatAssistantPortalFab(item, fab.hasAttribute('anchored'));

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
        isDragging = fab.hasAttribute('movable') && fab.hasAttribute('anchored');
        if (!isDragging) return;
        fab.classList.add('dragging');
        item.setPointerCapture(e.pointerId);
        item.style.transition = sizeTransition;
        initialPosition.x = position.x;
        initialPosition.y = position.y;
    });

    item.addEventListener('pointermove', (e) => {
        if (!isDragging) return;
        const itemRect = fab.getBoundingClientRect();
        // Candidate position from the right/bottom edges, keeping the FAB centered on the cursor.
        const nextX = screenWidth - e.clientX - (itemRect.width / 2);
        const nextY = screenHeight - e.clientY - (itemRect.height / 2);
        // Ignore movement below the sensitivity threshold, and do not mutate position for it, so a
        // click never nudges the FAB when it is later committed by snapToBoundary/updatePosition.
        if (Math.abs(nextX - initialPosition.x) < sensitivity
            && Math.abs(nextY - initialPosition.y) < sensitivity) {
            return;
        }
        position.x = nextX;
        position.y = nextY;
        updatePosition();
    });

    item.addEventListener('click', () => onFabClick());
    item.addEventListener('pointerup', (e) => stopDragging(e));
    item.addEventListener('pointerleave', (e) => stopDragging(e));
    item.addEventListener('pointercancel', (e) => stopDragging(e));

    function stopDragging(e) {
        if(isDragging) {
            isDragging = false;
            item.style.transition = snapTransition + ', ' + sizeTransition;
            fab.classList.remove('dragging');
            if (item.hasPointerCapture(e.pointerId)) {
                item.releasePointerCapture(e.pointerId);
            }
            snapToBoundary();
            if (isClickOnlyEvent()) {
                root.$server?.onClick();
            }
        }
    }

    function onFabClick() {
        if(!fab.hasAttribute('movable') || !fab.hasAttribute('anchored')) {
            root.$server?.onClick();
        }
    }

    // Apply the configured corner once the FAB has a real size. getBoundingClientRect returns 0
    // before the first layout pass (and while the FAB lives in a hidden tab), which would push it
    // off-screen for any corner other than the bottom-right default. An IntersectionObserver fires
    // when the FAB becomes visible, so the size is known by then.
    function applyCorner() {
        const start = fcChatAssistantCornerPosition(item, fab, positionRaw, margin);
        position.x = start.x;
        position.y = start.y;
        initialPosition.x = start.x;
        initialPosition.y = start.y;
        updatePosition();
    }
    if (fcChatAssistantSize(fab).width > 0) {
        applyCorner();
    } else {
        const observer = new IntersectionObserver((_, obs) => {
            if (fcChatAssistantSize(fab).width > 0) {
                obs.disconnect();
                applyCorner();
            }
        });
        observer.observe(fab);
    }

    updatePosition();
};

// Moves the FAB back to the given corner, animating the transition like a drag-snap.
window.fcChatAssistantResetPosition = (item, marginRaw, positionRaw) => {
    const margin = parseFloat(marginRaw);
    const fab = item.querySelector('.fc-chat-assistant-fab') || item;
    const target = fcChatAssistantCornerPosition(item, fab, positionRaw, margin);
    // A gentle ease-out with a small overshoot (1.1 vs the snappier 1.275 used while dragging) so the
    // reset settles into the corner without bouncing.
    const resetTransition = 'all 0.45s cubic-bezier(0.22, 0.61, 0.36, 1.1)';
    item.style.transition = resetTransition;
    item.style.right = target.x + 'px';
    item.style.bottom = target.y + 'px';
    // Keep the live drag state in sync so the next drag starts from the reset position.
    if (item.__fcPosition) {
        item.__fcPosition.x = target.x;
        item.__fcPosition.y = target.y;
    }
};

// Removes any active media-query listener, freezing the component in its current mode. Also clears
// the server-side listener guard so a later re-enable re-attaches cleanly.
window.fcChatAssistantMobileModeOff = (root) => {
    if (root.__fcMobileMql && root.__fcMobileHandler) {
        root.__fcMobileMql.removeEventListener('change', root.__fcMobileHandler);
    }
    root.__fcMobileMql = null;
    root.__fcMobileHandler = null;
    root['fc-chat-assistant-mobile-listener'] = null;
};

// Watches the viewport width against the given breakpoint and notifies the server whenever the
// mobile/desktop state changes. Fires once immediately so the initial mode matches the viewport.
// A breakpoint of 0 (or less) disables mobile mode entirely (always desktop).
window.fcChatAssistantMobileMode = (root, breakpointRaw) => {
    // Replace any previous listener so repeated calls (e.g. breakpoint changes) don't stack up.
    window.fcChatAssistantMobileModeOff(root);
    const breakpoint = parseFloat(breakpointRaw);
    if (!(breakpoint > 0)) {
        // Disabled: ensure desktop mode and register no listener.
        root.$server?.onMobileModeChange(false);
        return;
    }
    const mql = window.matchMedia('(max-width: ' + breakpoint + 'px)');
    const handler = (e) => root.$server?.onMobileModeChange(e.matches);
    mql.addEventListener('change', handler);
    root.__fcMobileMql = mql;
    root.__fcMobileHandler = handler;
    // Evaluate the current viewport so the initial mode is correct.
    handler(mql);
};

// Watches the CHAT WINDOW's own size (the given overlay Div, which fills the popover content) against a
// width and/or height threshold, identified by `key`. A null threshold means that axis is not tracked;
// when both are given they must both be satisfied (AND). The server is notified only when the
// above/below state actually flips (crossing), plus once on registration with the initial state, so a
// resize within one side of the threshold sends no calls. Multiple keys coexist independently.
window.fcChatAssistantScreenSize = (root, overlayDiv, key, widthRaw, heightRaw) => {
    if (!root.__fcScreenSizeListeners) {
        root.__fcScreenSizeListeners = {};
    }
    // Replace any previous registration under this key so repeated calls don't stack observers.
    window.fcChatAssistantScreenSizeOff(root, key);

    const width = parseFloat(widthRaw);
    const height = parseFloat(heightRaw);
    const isAbove = (rect) =>
        (Number.isNaN(width) || rect.width >= width)
        && (Number.isNaN(height) || rect.height >= height);

    const entry = { observer: null, last: null };
    const notify = (rect) => {
        // Ignore the pre-layout 0x0 state so the initial delivery reflects the real size.
        if (rect.width === 0 && rect.height === 0) {
            return;
        }
        const above = isAbove(rect);
        if (above !== entry.last) {
            entry.last = above;
            root.$server?.onScreenSizeChange(key, above);
        }
    };

    entry.observer = new ResizeObserver((entries) => notify(entries[0].contentRect));
    entry.observer.observe(overlayDiv);
    root.__fcScreenSizeListeners[key] = entry;
    // Deliver the current state immediately (if already laid out; otherwise the observer's first
    // callback delivers it).
    notify(overlayDiv.getBoundingClientRect());
};

// Removes the size observer registered under `key` (no-op if absent).
window.fcChatAssistantScreenSizeOff = (root, key) => {
    const listeners = root.__fcScreenSizeListeners;
    if (listeners && listeners[key]) {
        listeners[key].observer?.disconnect();
        delete listeners[key];
    }
    // Clear the refresh guard so the same key can be re-registered later.
    root['fc-chat-assistant-screen-size-' + key] = null;
};

// Removes every screen-size observer (used on disconnect).
window.fcChatAssistantScreenSizeOffAll = (root) => {
    const listeners = root.__fcScreenSizeListeners;
    if (listeners) {
        Object.keys(listeners).forEach((key) => {
            listeners[key].observer?.disconnect();
            // Clear the per-key refresh guard too (mirroring fcChatAssistantScreenSizeOff), otherwise it
            // would block re-registration of the same key after a detach/reattach.
            root['fc-chat-assistant-screen-size-' + key] = null;
        });
    }
    root.__fcScreenSizeListeners = {};
};
