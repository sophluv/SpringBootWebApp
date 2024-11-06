package app.client;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .subscribe(media -> {
                    try (FileWriter fileWriter = new FileWriter("mediaTitlesAndReleaseDates.txt", true)) {
                        fileWriter.write("Title: " + media.getTitle() + ", Release Date: " + media.getReleaseDate() + "\n");
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
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .subscribe(count -> {
                    try (FileWriter fileWriter = new FileWriter("countMediaItems.txt", true)) {
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
                .count()
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .subscribe(count -> {
                    try (FileWriter fileWriter = new FileWriter("highRatedMediaItems.txt", true)) {
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
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .subscribe(media -> {
                    try (FileWriter fileWriter = new FileWriter("mediaFromTheEighties.txt", true)) {
                        fileWriter.write("Media from the 80's: " + media + "\n");
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
                .collectList()
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .subscribe(ratings -> {
                    double average = ratings.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    double variance = ratings.stream().mapToDouble(r -> Math.pow(r - average, 2)).average().orElse(0.0);
                    double stdDeviation = Math.sqrt(variance);

                    try (FileWriter fileWriter = new FileWriter("mediaRatingsStats.txt", true)) {
                        fileWriter.write("Average rating: " + average + ", Standard deviation: " + stdDeviation + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private static void writeOldestMediaItemName(WebClient webClient) {
        webClient.get().uri("/media")
                .retrieve()
                .bodyToFlux(Media.class)
                .sort((m1, m2) -> m1.getReleaseDate().compareTo(m2.getReleaseDate()))
                .next()
                .map(Media::getTitle)
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .subscribe(title -> {
                    try (FileWriter fileWriter = new FileWriter("oldestMediaItem.txt", true)) {
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

                    try (FileWriter fileWriter = new FileWriter("averageUsersPerMedia.txt", true)) {
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
                        .collectList()
                        .map(medias -> "User: " + user.getName() + ", Subscribed Media: " +
                                medias.stream().map(Media::getTitle).collect(Collectors.joining(", ")))
                )
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .subscribe(userData -> {
                    try (FileWriter fileWriter = new FileWriter("userSubscribedMedia.txt", true)) {
                        fileWriter.write(userData + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
}
