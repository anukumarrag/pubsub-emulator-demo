package nhartner.demo.pubsub;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gcp.autoconfigure.pubsub.GcpPubSubProperties;
import org.springframework.cloud.gcp.pubsub.PubSubAdmin;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.support.AcknowledgeablePubsubMessage;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.concurrent.ListenableFuture;

import com.google.api.client.util.Value;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.Topic;

@SpringBootTest(classes = { PubsubEmulatorDemoApplication.class })
@ActiveProfiles("unit-test")
public class PubsubEmulatorDemoApplicationTests {

	@Autowired
	private PubSubAdmin pubSubAdmin;

	@Autowired
	private PubSubTemplate pubSubTemplate;
	
	@Autowired
	private GcpPubSubProperties gcpPubSubProperties;

	@Test
	public void testForPubSubTemplate() throws InterruptedException, ExecutionException {

		// create Topic
		ProjectTopicName topicName = ProjectTopicName.of(gcpPubSubProperties.getProjectId(), "sample-topic");
		Topic topic = pubSubAdmin.createTopic(topicName.getTopic());
		assertNotNull(topic);
		assertEquals(topicName.toString(), topic.getName());
		
		
		// Create subscription
		ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(gcpPubSubProperties.getProjectId(), "sample-topic-sub");
		Subscription subscription =  pubSubAdmin.createSubscription(subscriptionName.getSubscription(), topicName.getTopic());
		assertNotNull(subscription);
		assertEquals(subscriptionName.toString(), subscription.getName());
		
		// publish message to topic

		String message = "Hello Test Message "+System.currentTimeMillis();
		ListenableFuture<String> resp = pubSubTemplate.publish(topicName.getTopic(), message);
		resp.get();
		
		// pull message from subscription


		List<AcknowledgeablePubsubMessage> pulledMsg = pubSubTemplate.pull(subscriptionName.getSubscription(), 10, true);
		
		assertNotNull(pulledMsg);
		assertFalse(pulledMsg.isEmpty());
		assertEquals(pulledMsg.get(0).getPubsubMessage().getData().toStringUtf8(), message);
		
		
	}


}
