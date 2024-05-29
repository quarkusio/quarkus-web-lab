package web.lab.comments;

import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

@ApplicationScoped
public class Startup {
    /**
     * This method is executed at the start of your application
     */
    @Transactional
    public void start(@Observes StartupEvent evt) {
        // in DEV mode we seed some data
        if (LaunchMode.current() == LaunchMode.DEVELOPMENT) {
            new Comment("test", LocalDateTime.of(2024, 3, 3, 10, 22), "JohnDoeDev", """
                    **Wow, this new Quarkus update is incredible!** 
                    
                    The performance improvements are exactly what I needed for my current project. 
                    The integration with Kubernetes is seamless and saves so much time. 
                                        
                    Kudos to the team for this fantastic release! 🎉
                    """).persist();

            new Comment("test", LocalDateTime.of(2024, 5, 1, 18, 32), "CodeGuru", """
                    While I appreciate the new features, especially the improved memory usage, I feel like the **documentation could be better**.
                    
                    Some parts are quite vague and require a lot of trial and error to understand. 
                                        
                    Any plans to enhance the docs soon?
                    """).persist();

            new Comment("test", LocalDateTime.of(2024, 1, 10, 8, 10), "TechEnthusiast", """
                    This update seems interesting.
                    
                    I haven't used Quarkus much, but these performance claims are impressive.
                    **Might give it a try for my next microservice project.
                    Does anyone have experience with Quarkus in production?**
                    """).persist();

            new Comment("test", LocalDateTime.of(2024, 2, 28, 10, 35), "JavaNoob", """
                    **Quarkus is overrated.** 
                    
                    Spring Boot has been here for years and I won't change.           
                    """).persist();

            new Comment("test", LocalDateTime.of(2024, 4, 22, 12, 22), "DevNewbie", """
                    I’m new to Quarkus and this update sounds promising. 
                    Can someone explain how Quarkus compares to traditional Java EE applications in terms of startup time and resource consumption? 
                    Trying to decide if it's worth learning.
                    """).persist();

            new Comment("test", LocalDateTime.now(), "SkepticalCoder", """
                    I’ve heard these performance promises before.
                    Show me real-world benchmarks and production success stories. Until then, I'll reserve my judgment.
                    """).persist();

        }
    }
}