package ca.gbc.notificationservice.service;

import ca.gbc.notificationservice.event.PendingEventPlacedEvent;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final JavaMailSender javaMailSender;

    @KafkaListener(topics = "event-created")
    public void listen(PendingEventPlacedEvent event) {
            log.info("Received message from event-created topic {}", event);

        MimeMessagePreparator mimeMessagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper =  new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("comp3095@georgebrown.ca");
            messageHelper.setTo(event.getMail());
            messageHelper.setSubject(String.format("Your event (%s) was places successfullu",
                    event.getEventName()));
            messageHelper.setText(String.format("""
                    Good Day.
                    
                    Your event with Event name was successfully placed
                    
                    Thank you for your business
                    COMP3095 Staff
                    """, event.getEventName()

            ));
        };

        try{
            javaMailSender.send(mimeMessagePreparator);
            log.info("event notification successfully sent !");
        }catch(MailException e){
            log.error("Exception while sending mail", e);
            throw new RuntimeException("Exception occurred when attempting to send mail", e);
        }
    }

}
