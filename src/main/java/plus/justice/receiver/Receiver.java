package plus.justice.receiver;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import plus.justice.models.QueueMessage;

@Component
@PropertySource("classpath:config.properties")
public class Receiver {
    @RabbitListener(queues = "${justice.rabbitmq.queue.name}")
    public void handleMessage(QueueMessage message) throws InterruptedException {
        System.out.println("Received: <" + message.getId() + ">");
    }
}