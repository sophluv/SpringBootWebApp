package app.client;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.function.client.WebClient;

import app.client.model.Media;
import app.client.model.User;
import app.client.model.UserMedia;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@SpringBootApplication
public class WebClientApplication {

    public static void main(String[] args) {
        WebClient webClient = WebClient.create("http://localhost:8080"); 
        writeAllMediaTitlesAndReleaseDates(webClient);
        writeTotalCountOfMediaItems(webClient);
        writeMediaItemsWithHighRatings(webClient);
        writeMediaFromTheEighties(webClient);
        writeOldestMediaItemName(webClient);
        writeAverageAndStandardDeviationOfMediaRatings(webClient);
        writeAverageNumberOfUsersPerMedia(webClient);
        writeUserDataWithSubscribedMedia(webClient);
        
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.out.println("An error occurred during sleep.");
            e.printStackTrace();
        }
    }

    private static void writeAllMediaTitlesAndReleaseDates(WebClient webClient) {
        webClient.get().uri("/media")
                .retrieve()
                .bodyToFlux(Media.class)
                .reduce("", (acc, media) -> acc + "Title: " + media.getTitle() + ", Release Date: " + media.getReleaseDate() + "\n")
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .subscribe(content -> {
                    try (FileWriter fileWriter = new FileWriter("mediaTitlesAndReleaseDates.txt", false)) {
                        fileWriter.write(content);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
    

    private static void writeTotalCountOfMediaItems(WebClient webClient) {
        webClient.get().uri("/media")
                .retrieve()
                .bodyToFlux(Media.class)
                .count()
                .switchIfEmpty(Mono.fromRunnable(() -> {
                    try (FileWriter fileWriter = new FileWriter("countMediaItems.txt", false)) {
                        fileWriter.write("No media items found.\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }))
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .subscribe(count -> {
                    try (FileWriter fileWriter = new FileWriter("countMediaItems.txt", false)) {
                        fileWriter.write("Total count of media items: " + count + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
    
    private static void writeMediaItemsWithHighRatings(WebClient webClient) {
        webClient.get().uri("/media")
                .retrieve()
                .bodyToFlux(Media.class)
                .filter(media -> media.getAverageRating() > 8)
                .reduce(0L, (count, media) -> count + 1)
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .subscribe(count -> {
                    try (FileWriter fileWriter = new FileWriter("highRatedMediaItems.txt", false)) {
                        fileWriter.write("Total count of media items with rating > 8: " + count + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
    
    private static void writeMediaFromTheEighties(WebClient webClient) {
        webClient.get().uri("/media")
                .retrieve()
                .bodyToFlux(Media.class)
                .filter(media -> media.getReleaseDate().isAfter(LocalDate.of(1980, 1, 1)) &&
                                 media.getReleaseDate().isBefore(LocalDate.of(1989, 12, 31)))
                .sort((m1, m2) -> Double.compare(m2.getAverageRating(), m1.getAverageRating()))
                .reduce("", (acc, media) -> acc + "Title: " + media.getTitle() + ", Rating: " + media.getAverageRating() + "\n")
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .subscribe(content -> {
                    try (FileWriter fileWriter = new FileWriter("mediaFromTheEighties.txt", false)) {
                        fileWriter.write(content);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
    

    private static void writeAverageAndStandardDeviationOfMediaRatings(WebClient webClient) {
        webClient.get().uri("/media")
                .retrieve()
                .bodyToFlux(Media.class)
                .map(Media::getAverageRating)
                .reduce(new double[]{0.0, 0.0}, (acc, rating) -> {
                    acc[0] += rating;  
                    acc[1] += 1;       
                    return acc;
                })
                .map(acc -> {
                    double average = acc[0] / acc[1];
                    return new double[]{average, acc[0]}; 
                })
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .subscribe(stats -> {
                    try (FileWriter fileWriter = new FileWriter("mediaRatingsStats.txt", false)) {
                        fileWriter.write("Average rating: " + stats[0] + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
    
    private static void writeOldestMediaItemName(WebClient webClient) {
        webClient.get().uri("/media")
                .retrieve()
                .bodyToFlux(Media.class)
                .reduce((oldest, media) -> media.getReleaseDate().isBefore(oldest.getReleaseDate()) ? media : oldest)
                .map(Media::getTitle)
                .switchIfEmpty(Mono.just("No media items available"))
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .subscribe(title -> {
                    try (FileWriter fileWriter = new FileWriter("oldestMediaItem.txt", false)) {
                        fileWriter.write("Oldest media item: " + title + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
    
    private static void writeAverageNumberOfUsersPerMedia(WebClient webClient) {
        Flux<UserMedia> userMediaFlux = webClient.get()
                .uri("/user-media")
                .retrieve()
                .bodyToFlux(UserMedia.class);
    
        Flux<Media> mediaFlux = webClient.get()
                .uri("/media")
                .retrieve()
                .bodyToFlux(Media.class);
    
        mediaFlux
                .flatMap(media -> userMediaFlux.filter(userMedia -> userMedia.getMediaId().equals(media.getId())).count())
                .reduce((totalUsers, mediaUserCount) -> totalUsers + mediaUserCount)  
                .zipWith(mediaFlux.count()) 
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .subscribe(tuple -> {
                    long totalUserCount = tuple.getT1();  
                    long mediaCount = tuple.getT2(); 
                    double averageUsersPerMedia = mediaCount == 0 ? 0 : (double) totalUserCount / mediaCount;
    
                    try (FileWriter fileWriter = new FileWriter("averageUsersPerMedia.txt", false)) {
                        fileWriter.write("Average number of users per media item: " + averageUsersPerMedia + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
    
    
    private static void writeUserDataWithSubscribedMedia(WebClient webClient) {
        // Fetch all user-media relationships
        webClient.get().uri("/user-media")
                .retrieve()
                .bodyToFlux(UserMedia.class)
                .flatMap(userMedia -> 
                    webClient.get().uri("/users/{id}", userMedia.getUserId())
                            .retrieve()
                            .bodyToMono(User.class)
                            .zipWith(
                                webClient.get().uri("/media/{id}", userMedia.getMediaId())
                                        .retrieve()
                                        .bodyToMono(Media.class),
                                (user, media) -> {
                                    return "User: " + user.getName() + ", Subscribed Media: " + media.getTitle();
                                }
                            )
                )
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .subscribe(userData -> {
                    try (FileWriter fileWriter = new FileWriter("userSubscribedMedia.txt", false)) { 
                        fileWriter.write(userData + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
    


}
