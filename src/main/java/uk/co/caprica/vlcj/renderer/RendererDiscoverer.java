package uk.co.caprica.vlcj.renderer;

import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.binding.internal.*;
import uk.co.caprica.vlcj.player.events.renderer.RendererDiscovererEvent;
import uk.co.caprica.vlcj.player.events.renderer.RendererDiscovererEventFactory;
import uk.co.caprica.vlcj.player.events.renderer.RendererDiscovererEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// FIXME not too happy with having to pass LibVlc down through the listener

// The native API doc implies the event handler has to call HOLD if it wants to use the item, but the API doc also
// says the item is valid until you get the same pointer in a deleted callback
// so i think you can probably choose to hold at any time TBH
// we will need to expose the item so it can be set on a mediaplayer

public class RendererDiscoverer {

    private final LibVlc libvlc;

    private final libvlc_renderer_discoverer_t discovererInstance;

    /**
     * Collection of media player event listeners.
     * <p>
     * A {@link CopyOnWriteArrayList} is used defensively so as not to interfere with the processing of any existing
     * events that may be being generated by the native callback in the unlikely case that a listeners is being added or
     * removed.
     */
    private final List<RendererDiscovererEventListener> eventListenerList = new CopyOnWriteArrayList<RendererDiscovererEventListener>();

    /**
     * Call-back to handle native media player events.
     */
    private final DiscovererCallback callback = new DiscovererCallback();

    public RendererDiscoverer(LibVlc libvlc, libvlc_renderer_discoverer_t discoverer) {
        this.libvlc             = libvlc;
        this.discovererInstance = discoverer;

        registerNativeEventListener();
    }

    public void addRendererDiscovererEventListener(RendererDiscovererEventListener listener) {
        eventListenerList.add(listener);
    }

    public void removeRendererDiscovererEventListener(RendererDiscovererEventListener listener) {
        eventListenerList.remove(listener);
    }

    public boolean start() {
        return libvlc.libvlc_renderer_discoverer_start(discovererInstance) == 0;
    }

    public void stop() {
        libvlc.libvlc_renderer_discoverer_stop(discovererInstance);
    }

    public void release() {
        eventListenerList.clear();
        deregisterNativeEventListener();
        libvlc.libvlc_renderer_discoverer_release(discovererInstance);
    }

    private void registerNativeEventListener() {
        libvlc_event_manager_t rendererDiscovererEventManager = libvlc.libvlc_renderer_discoverer_event_manager(discovererInstance);
        for (libvlc_event_e event : libvlc_event_e.values()) {
            if (event.intValue() >= libvlc_event_e.libvlc_RendererDiscovererItemAdded.intValue() && event.intValue() <= libvlc_event_e.libvlc_RendererDiscovererItemDeleted.intValue()) {
                libvlc.libvlc_event_attach(rendererDiscovererEventManager, event.intValue(), callback, null);
            }
        }
    }

    private void deregisterNativeEventListener() {
        libvlc_event_manager_t rendererDiscovererEventManager = libvlc.libvlc_renderer_discoverer_event_manager(discovererInstance);
        for (libvlc_event_e event : libvlc_event_e.values()) {
            if (event.intValue() >= libvlc_event_e.libvlc_RendererDiscovererItemAdded.intValue() && event.intValue() <= libvlc_event_e.libvlc_RendererDiscovererItemDeleted.intValue()) {
                libvlc.libvlc_event_detach(rendererDiscovererEventManager, event.intValue(), callback, null);
            }
        }
    }

    /**
     * Raise a new event (dispatch it to listeners).
     * <p>
     * Events are processed on the <em>native</em> callback thread, so must execute quickly and certainly must never
     * block.
     * <p>
     * It is also generally <em>forbidden</em> for an event handler to call back into LibVLC.
     *
     * @param rendererDiscovererEvent event to raise, may be <code>null</code> and if so will be ignored
     */
    void raiseEvent(RendererDiscovererEvent rendererDiscovererEvent) {
        if (rendererDiscovererEvent != null) {
            for (RendererDiscovererEventListener listener : eventListenerList) {
                rendererDiscovererEvent.notify(listener);
            }
        }
    }

    private class DiscovererCallback implements libvlc_callback_t {

        private DiscovererCallback() {
            Native.setCallbackThreadInitializer(this, new CallbackThreadInitializer(true, false, "renderer-discoverer-events"));
        }

        @Override
        public void callback(libvlc_event_t event, Pointer userData) {
            raiseEvent(RendererDiscovererEventFactory.createEvent(libvlc, RendererDiscoverer.this, event));
        }

    }

}
