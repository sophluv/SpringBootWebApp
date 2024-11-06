package app.client;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.function.client.WebClient;

import app.client.model.Media;
import app.client.model.User;
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
                    acc[0] += rating;   // Sum of ratings
                    acc[1] += 1;        // Count of ratings
                    return acc;
                })
                .map(acc -> {
                    double average = acc[0] / acc[1];
                    return new double[]{average, acc[0]}; // Temporarily return average for next processing
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
        Mono<List<Media>> mediaList = webClient.get()
                .uri("/media")
                .retrieve()
                .bodyToFlux(Media.class)
                .collectList();

        Mono<List<User>> userList = webClient.get()
                .uri("/users")
                .retrieve()
                .bodyToFlux(User.class)
                .collectList();

        Mono.zip(mediaList, userList)
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .subscribe(tuple -> {
                    List<Media> media = tuple.getT1();
                    List<User> users = tuple.getT2();
                    double averageUsersPerMedia = media.isEmpty() ? 0 : users.size() / (double) media.size();

                    try (FileWriter fileWriter = new FileWriter("averageUsersPerMedia.txt", false)) {
                        fileWriter.write("Average number of users per media item: " + averageUsersPerMedia + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private static void writeUserDataWithSubscribedMedia(WebClient webClient) {
        webClient.get().uri("/users")
                .retrieve()
                .bodyToFlux(User.class)
                .flatMap(user -> webClient.get()
                        .uri("/media?userId=" + user.getId())
                        .retrieve()
                        .bodyToFlux(Media.class)
                        .map(Media::getTitle)
                        .reduce((title1, title2) -> title1 + ", " + title2)
                        .map(titles -> "User: " + user.getName() + ", Subscribed Media: " + titles)
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
