package nur.edu.nurtricenter_patient.infraestructure.inbound;

import java.util.List;
import java.util.ArrayList;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@Configuration
@EnableRabbit
@EnableConfigurationProperties(InboundSubscriptionProperties.class)
public class InboundRabbitConfig {

  @Bean
  public Declarables inboundRabbitDeclarables(InboundSubscriptionProperties props) {
    if (!props.isEnabled() || props.getQueue() == null || props.getQueue().isBlank() || props.getExchange() == null || props.getExchange().isBlank()) {
      return new Declarables();
    }

    Exchange exchange = buildExchange(props);
    Queue queue = new Queue(
      props.getQueue(),
      props.isQueueDurable(),
      props.isQueueExclusive(),
      props.isQueueAutoDelete()
    );

    List<String> routingKeys = props.getRoutingKeys();
    if (routingKeys == null || routingKeys.isEmpty()) {
      return new Declarables(exchange, queue);
    }

    List<Declarable> items = new ArrayList<>();
    items.add(exchange);
    items.add(queue);
    for (String key : routingKeys) {
      String routingKey = key != null ? key.trim() : "";
      if (routingKey.isBlank()) {
        continue;
      }
      Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey).noargs();
      items.add(binding);
    }

    return new Declarables(items);
  }

  private Exchange buildExchange(InboundSubscriptionProperties props) {
    String type = props.getExchangeType() != null ? props.getExchangeType().trim().toLowerCase() : "topic";
    String name = props.getExchange();
    boolean durable = props.isExchangeDurable();
    return switch (type) {
      case "fanout" -> new FanoutExchange(name, durable, false);
      case "direct" -> new DirectExchange(name, durable, false);
      case "headers" -> new HeadersExchange(name, durable, false);
      default -> new TopicExchange(name, durable, false);
    };
  }
}
