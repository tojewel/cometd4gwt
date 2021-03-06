package org.cometd4gwt.client;

import java.util.HashMap;
import java.util.Map;

import org.cometd4gwt.client.impl.Subscription;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * On any sort of disconnect, server just un-subscribes all the channels that
 * this client is currently subscribed. This class stores all the subscription
 * of a client and re-subscribe all the subscription on reconnect
 */
public class AutoReSubscribableClient implements CometdClient {

	private final CometdClient cometdClient;
	private boolean isReconnecting = false;

	private final Map<JavaScriptObject, SubData> subscriptions = new HashMap<JavaScriptObject, SubData>();

	public AutoReSubscribableClient(final CometdClient cometdClient) {
		this.cometdClient = cometdClient;

		cometdClient.addConnectionListener(new CometConnectionListener() {
			@Override
			public void onConnected() {
				if (isReconnecting) {
					cometdClient.startBatch();

					for (JavaScriptObject subscription : subscriptions.keySet()) {
						if (subscription != null) {
							cometdClient.unsubscribe(subscription);
							SubData subData = subscriptions.get(subscription);
							cometdClient.addSubscriber(subData.getChannel(), subData.getConsumer());
						}
					}

					cometdClient.endBatch();
				}
			}

			@Override
			public void onDisconnected() {
				isReconnecting = true;
			}
		});
	}

	@Override
	public JavaScriptObject addSubscriber(String channel, CometMessageConsumer consumer) {
		JavaScriptObject subscription = cometdClient.addSubscriber(channel, consumer);
		subscriptions.put(subscription, new SubData(channel, consumer));
		return subscription;
	}

	@Override
	public JavaScriptObject addSubscriber(String channel, CometMessageConsumer consumer, JsoListener<Subscription> listener) {
		JavaScriptObject subscription = cometdClient.addSubscriber(channel, consumer, listener);
		subscriptions.put(subscription, new SubData(channel, consumer));
		return subscription;
	}

	@Override
	public void unsubscribe(JavaScriptObject subscription) {
		cometdClient.unsubscribe(subscription);
		subscriptions.remove(subscription);
	}

	class SubData {
		private final String channel;
		private final CometMessageConsumer consumer;

		public SubData(String channel, CometMessageConsumer consumer) {
			this.channel = channel;
			this.consumer = consumer;
		}

		public String getChannel() {
			return channel;
		}

		public CometMessageConsumer getConsumer() {
			return consumer;
		}
	}

	// following are just delegates ------

	@Override
	public void addConnectionListener(CometConnectionListener connectionListener) {
		cometdClient.addConnectionListener(connectionListener);
	}

	@Override
	public void addListener(String channel, JsoListener<? extends JavaScriptObject> receiver) {
		cometdClient.addListener(channel, receiver);
	}

	@Override
	public void addScriptLoadListener(ScriptLoadListener scriptLoadListener) {
		cometdClient.addScriptLoadListener(scriptLoadListener);
	}

	@Override
	public void connect(ConnectionConfig config) {
		cometdClient.connect(config);
	}

	@Override
	public void disconnectAsync() {
		cometdClient.disconnectAsync();
	}

	@Override
	public boolean isConnected() {
		return cometdClient.isConnected();
	}

	@Override
	public void publish(String channelId, IsSerializable message, AsyncCallback<Void> callback) {
		cometdClient.publish(channelId, message, callback);
	}

	@Override
	public void publish(String channelId, IsSerializable message, String clientId, AsyncCallback<Void> callback) {
		cometdClient.publish(channelId, message, clientId, callback);
	}

	@Override
	public void endBatch() {
		cometdClient.endBatch();
	}

	@Override
	public void startBatch() {
		cometdClient.startBatch();
	}
}