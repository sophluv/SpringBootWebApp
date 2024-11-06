package app.client;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.function.client.WebClient;

import app.client.model.Media;
import app.client.model.User;
import app.client.util.Stats;
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
        writeAverageNumberOfUsersPerMedia(webClient);
        writeUserDataWithSubscribedMedia(webClient);
        
        // Delay to allow asynchronous tasks to complete before the application exits
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
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))  // Retry up to 3 times with backoff
                .subscribe(media -> {
                    try (FileWriter fileWriter = new FileWriter("mediaTitlesAndReleaseDates.txt", true)) {
                        fileWriter.write("Title: " + media.getTitle() + ", Release Date: " + media.getReleaseDate() + "\n");
                        System.out.println("Media title and release date written");
                    } catch (IOException e) {
                        System.out.println("An IOException was thrown");
                        e.printStackTrace();
                    }
                });
    }

    private static void writeTotalCountOfMediaItems(WebClient webClient) {
        webClient.get().uri("/media")
                .retrieve()
                .bodyToFlux(Media.class)
                .count()
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))  // Retry up to 3 times with backoff
                .subscribe(count -> {
                    try (FileWriter fileWriter = new FileWriter("countMediaItems.txt", true)) {
                        fileWriter.write("Total count of media items: " + count + "\n");
                        System.out.println("Total count added to file");
                    } catch (IOException e) {
                        System.out.println("An IOException was thrown");
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
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))  // Retry up to 3 times with backoff
                .subscribe(count -> {
                    try (FileWriter fileWriter = new FileWriter("highRatedMediaItems.txt", true)) {
                        fileWriter.write("Total count of media items with rating > 8: " + count + "\n");
                        System.out.println("High ratings count added to file");
                    } catch (IOException e) {
                        System.out.println("An IOException was thrown");
                        e.printStackTrace();
                    }
                });
    }

    private static void writeMediaFromTheEighties(WebClient webClient) {
        webClient.get().uri("/media")
                .retrieve()
                .bodyToFlux(Media.class)
                .filter(media -> media.getReleaseDate().isAfter(LocalDate.of(1980, 1, 1))
                        && media.getReleaseDate().isBefore(LocalDate.of(1989, 12, 31)))
                .sort((m1, m2) -> Double.compare(m2.getAverageRating(), m1.getAverageRating()))
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))  // Retry up to 3 times with backoff
                .subscribe(media -> {
                    try (FileWriter fileWriter = new FileWriter("mediaFromTheEighties.txt", true)) {
                        fileWriter.write("Media from the 80's: " + media + "\n");
                        System.out.println("Media from the 80's written");
                    } catch (IOException e) {
                        System.out.println("An IOException was thrown");
                        e.printStackTrace();
                    }
                });
    }

    private static void writeAverageAndStandardDeviationOfMediaRatings(WebClient webClient) {
        webClient.get().uri("/media")
                .retrieve()
                .bodyToFlux(Media.class)
                .map(Media::getAverageRating)
                .reduce(new ArrayList<Double>(), (list, rating) -> {
                    list.add(rating);
                    return list;
                })
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))  // Retry up to 3 times with backoff
                .subscribe(ratings -> {
                    double average = Stats.calcularMedia(ratings);
                    double stdDeviation = Stats.calcularDesvioPadrao(ratings);

                    try (FileWriter fileWriter = new FileWriter("util.stats", true)) {
                        fileWriter.write("Average rating: " + average + ", Standard deviation: " + stdDeviation + "\n");
                        System.out.println("Media ratings statistics written");
                    } catch (IOException e) {
                        System.out.println("An IOException was thrown");
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
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))  // Retry up to 3 times with backoff
                .subscribe(title -> {
                    try (FileWriter fileWriter = new FileWriter("oldestMediaItem.txt", true)) {
                        fileWriter.write("Oldest media item: " + title + "\n");
                        System.out.println("Oldest media item written");
                    } catch (IOException e) {
                        System.out.println("An IOException was thrown");
                        e.printStackTrace();
                    }
                });
    }

    private static void writeAverageNumberOfUsersPerMedia(WebClient webClient) {
        Mono<List<Media>> mediaList = webClient.get()
                .uri("/media")
                .retrieve()
                .bodyToFlux(Media.class)
                .reduce(new ArrayList<Media>(), (list, media) -> {
                    list.add(media);
                    return list;
                });

        Mono<List<User>> userList = webClient.get()
                .uri("/users")
                .retrieve()
                .bodyToFlux(User.class)
                .reduce(new ArrayList<User>(), (list, user) -> {
                    list.add(user);
                    return list;
                });

        Mono.zip(mediaList, userList)
                .flatMap(tuple -> {
                    List<Media> media = tuple.getT1();
                    List<User> users = tuple.getT2();
                    double averageUsersPerMedia = media.isEmpty() ? 0 : users.size() / (double) media.size();
                    return Mono.just(averageUsersPerMedia);
                })
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))  // Retry up to 3 times with backoff
                .subscribe(avg -> {
                    try (FileWriter fileWriter = new FileWriter("util.stats", true)) {
                        fileWriter.write("Average number of users per media item: " + avg + "\n");
                        System.out.println("Average number of users per media item written");
                    } catch (IOException e) {
                        System.out.println("An IOException was thrown");
                        e.printStackTrace();
                    }
                });
    }

    private static void writeUserDataWithSubscribedMedia(WebClient webClient) {
        webClient.get().uri("/users")
                .retrieve()
                .bodyToFlux(User.class)
                .flatMap(user -> webClient.get()
                        .uri("/media?userId=" + user.getId()) // Ajuste de URI conforme necessário
                        .retrieve()
                        .bodyToFlux(Media.class)  // Flux de Media (múltiplos objetos de Media)
                        .reduce(new StringBuilder(), (acc, media) -> {
                            // Acumula os títulos das mídias no StringBuilder
                            if (acc.length() > 0) {
                                acc.append(", ");  // Adiciona vírgula separadora
                            }
                            acc.append(media.getTitle());  // Adiciona o título da mídia
                            return acc;  // Retorna o acumulador para o próximo ciclo
                        })
                        .map(acc -> "User: " + user.getName() + ", Subscribed Media: " + acc.toString())  // Converte o StringBuilder para String
                )
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))  // Retry up to 3 times with backoff
                .subscribe(userData -> {
                    try (FileWriter fileWriter = new FileWriter("util.stats", true)) {
                        fileWriter.write(userData + "\n");
                        System.out.println("User data with subscribed media written");
                    } catch (IOException e) {
                        System.out.println("An IOException was thrown");
                        e.printStackTrace();
                    }
                });
    }
}
