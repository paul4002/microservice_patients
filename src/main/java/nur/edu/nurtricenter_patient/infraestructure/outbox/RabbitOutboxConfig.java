package nur.edu.nurtricenter_patient.infraestructure.outbox;

import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OutboxPublisherProperties.class)
public class RabbitOutboxConfig {

  @Bean
  public ConnectionFactory rabbitConnectionFactory(OutboxPublisherProperties props) {
    CachingConnectionFactory factory = new CachingConnectionFactory();
    factory.setHost(props.getHost());
    factory.setPort(props.getPort());
    factory.setUsername(props.getUsername());
    factory.setPassword(props.getPassword());
    factory.setVirtualHost(props.getVhost());
    factory.setConnectionTimeout(props.getConnectTimeoutSeconds() * 1000);
    return factory;
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, OutboxPublisherProperties props) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setReplyTimeout(props.getReadWriteTimeoutSeconds() * 1000L);
    return template;
  }

  @Bean
  public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
    RabbitAdmin admin = new RabbitAdmin(connectionFactory);
    admin.setAutoStartup(true);
    return admin;
  }

  @Bean
  public Declarables rabbitOutboxDeclarables(OutboxPublisherProperties props) {
    Exchange exchange = buildExchange(props);

    List<Declarable> declarables = new ArrayList<>();
    declarables.add(exchange);

    List<String> queueNames = resolveQueueNames(props.getQueue());
    if (queueNames.isEmpty()) {
      Queue queue = buildQueue(props, props.getQueue());
      declarables.add(queue);
      declarables.add(buildBinding(queue, exchange, props.getBindingKey()));
    } else {
      for (String queueName : queueNames) {
        Queue queue = buildQueue(props, queueName);
        declarables.add(queue);
        declarables.add(buildBinding(queue, exchange, queueName));
      }
    }
    return new Declarables(declarables);
  }

  private Exchange buildExchange(OutboxPublisherProperties props) {
    String type = props.getExchangeType() != null ? props.getExchangeType().toLowerCase() : "fanout";
    return switch (type) {
      case "topic" -> ExchangeBuilder.topicExchange(props.getExchange()).durable(props.isExchangeDurable()).build();
      case "direct" -> ExchangeBuilder.directExchange(props.getExchange()).durable(props.isExchangeDurable()).build();
      case "fanout" -> ExchangeBuilder.fanoutExchange(props.getExchange()).durable(props.isExchangeDurable()).build();
      default -> ExchangeBuilder.directExchange(props.getExchange()).durable(props.isExchangeDurable()).build();
    };
  }

  private Binding buildBinding(Queue queue, Exchange exchange, String bindingKey) {
    if (exchange instanceof FanoutExchange fanoutExchange) {
      return BindingBuilder.bind(queue).to(fanoutExchange);
    }
    if (exchange instanceof TopicExchange topicExchange) {
      return BindingBuilder.bind(queue).to(topicExchange).with(bindingKey != null ? bindingKey : queue.getName());
    }
    if (exchange instanceof DirectExchange directExchange) {
      return BindingBuilder.bind(queue).to(directExchange).with(bindingKey != null ? bindingKey : queue.getName());
    }
    return BindingBuilder.bind(queue).to(exchange).with(bindingKey != null ? bindingKey : "").noargs();
  }

  private Queue buildQueue(OutboxPublisherProperties props, String name) {
    QueueBuilder builder = QueueBuilder.durable(name);
    if (props.isQueueExclusive()) {
      builder = builder.exclusive();
    }
    if (props.isQueueAutoDelete()) {
      builder = builder.autoDelete();
    }
    return builder.build();
  }

  private List<String> resolveQueueNames(String raw) {
    if (raw == null || raw.isBlank()) {
      return List.of();
    }
    if (!raw.contains(",")) {
      return List.of();
    }
    List<String> names = new ArrayList<>();
    for (String part : raw.split(",")) {
      String trimmed = part.trim();
      if (!trimmed.isEmpty()) {
        names.add(trimmed);
      }
    }
    return names;
  }
}
