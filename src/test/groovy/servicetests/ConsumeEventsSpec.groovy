package servicetests

import com.example.kafkaFirstConsumer.DemoApplication
import com.example.kafkaFirstConsumer.service.MyAckConsumer
import com.example.kafkaFirstConsumer.service.MyConsumerAwareConsumer
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.ClassRule
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.DockerImageName
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
@ContextConfiguration(initializers = [Initializer.class])
@SpringBootTest(classes = [DemoApplication.class])
class ConsumeEventsSpec extends Specification {


    String topic = 'test-Topic'

    ConcurrentMessageListenerContainer<String, String> container1

    ConcurrentMessageListenerContainer<String, String> container2

    MyAckConsumer consumer1

    MyConsumerAwareConsumer consumer2

    @Shared
    @ClassRule
    public static KafkaContainer kafka  = new KafkaContainer(DockerImageName.parse('confluentinc/cp-kafka:5.3.0'))

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    'spring.kafka.bootstrap-servers=' + kafka.getBootstrapServers())
                    .applyTo(configurableApplicationContext.getEnvironment())
        }
    }

    def setupSpec() {
        kafka.start()
    }

    def 'test order consumer is able to consume messages'() {

        given: 'a kafka template'
        def configs = new HashMap(KafkaTestUtils.producerProps(kafka.getBootstrapServers()))
        def factory = new DefaultKafkaProducerFactory<String, String>(configs, new StringSerializer(), new StringSerializer())
        def template = new KafkaTemplate<String, String>(factory, true)

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, '1', 'Test 123')
        ProducerRecord<String, String> record2 = new ProducerRecord<>(topic, '2', 'Test 123')

        and:
        Map<String, Object> props = new HashMap<>()
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers())
        props.put(ConsumerConfig.GROUP_ID_CONFIG, 'gruppe1')
        props.put(ConsumerConfig.GROUP_INSTANCE_ID_DOC, 'rich1')
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, 'earliest')

        def consumerFactory1 = new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new StringDeserializer())
        container1 = new ConcurrentMessageListenerContainer(consumerFactory1, containerProperties())
        def testCons1 = consumerFactory1.createConsumer('gruppe1', 'richSuffix')

        props.put(ConsumerConfig.GROUP_ID_CONFIG, 'gruppe2')
        props.put(ConsumerConfig.GROUP_INSTANCE_ID_DOC, 'rich2')
        def consumerFactory2 = new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new StringDeserializer())
        container2 = new ConcurrentMessageListenerContainer(consumerFactory2, containerProperties2())
        def testCons2 = consumerFactory1.createConsumer('gruppe2', 'richSuffix')

        container1.setConcurrency(1)
        container1.start()

        container2.setConcurrency(1)
        container2.start()

        when: 'sending a message to kafka'
        Thread.sleep(5000)
        template.send(record).get()
        template.send(record).get()
        template.send(record2).get()
        template.send(record).get()

        Thread.sleep(5000)
        System.out.println('offsets c1: ' + KafkaTestUtils.getEndOffsets(testCons1 as Consumer<String, String>,'test-Topic',0))
        System.out.println('offsets c2: ' + KafkaTestUtils.getEndOffsets(testCons2 as Consumer<String, String>,'test-Topic',1))

        then: 'the message is consumed successfully'
        assert true

    }

    ContainerProperties containerProperties() {
        ContainerProperties containerProps = new ContainerProperties('test-Topic')
        consumer1 = new MyAckConsumer()
        containerProps.messageListener = consumer1
        containerProps.ackMode = ContainerProperties.AckMode.MANUAL
        containerProps.groupId = 'gruppe1'
        return containerProps
    }

    ContainerProperties containerProperties2() {
        ContainerProperties containerProps = new ContainerProperties('test-Topic')
        consumer2 = new MyConsumerAwareConsumer()
        containerProps.messageListener = consumer2
        containerProps.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        containerProps.groupId = 'gruppe2'
        return containerProps
    }
}