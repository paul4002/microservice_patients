package nur.edu.nurtricenter_patient;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.CommandHandlers;
import an.awesome.pipelinr.NotificationHandlers;
import an.awesome.pipelinr.Pipeline;
import an.awesome.pipelinr.Pipelinr;
import an.awesome.pipelinr.Notification;

@SpringBootApplication
public class NurtricenterPatientApplication {

	public static void main(String[] args) {
		SpringApplication.run(NurtricenterPatientApplication.class, args);
	}

	@Bean
	Pipeline pipeline(ObjectProvider<Command.Handler> commandHandlers, ObjectProvider<Notification.Handler> notificationHandlers, ObjectProvider<Command.Middleware> middlewares) {
    return new Pipelinr()
      .with((CommandHandlers) commandHandlers::stream)
      .with((NotificationHandlers) notificationHandlers::stream)
      .with((Command.Middlewares) middlewares::orderedStream);
  }
}
