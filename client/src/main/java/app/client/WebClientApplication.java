package app.client;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        writeMediaThatIsSubscribed(webClient);
        writeMediaFromTheEighties(webClient);
        writeOldestMediaItemName(webClient);
        writeAverageAndStandardDeviationOfMediaRatings(webClient);
        writeAverageNumberOfUsersPerMedia(webClient);
        writeUserDataWithSubscribedMedia(webClient);
        writeCompleteUserDataWithSubscribedMedia(webClient); 



        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.out.println("An error occurred during sleep.");
            e.printStackTrace();
        }
    }

    //1 Titles and release dates of all media items
    private static void writeAllMediaTitlesAndReleaseDates(WebClient webClient) {
        webClient.get().uri("/media")
                .retrieve()
                .bodyToFlux(Media.class)
                .reduce("", (acc, media) -> acc + "Title: " + media.getTitle() + ", Release Date: " + media.getReleaseDate() + "\n")
                .retryWhen(Retry.max(3).doAfterRetry(x->System.out.println("Retrying")).onRetryExhaustedThrow((x,y)-> {
                    System.out.println("Retries exhausted");
                    return new RuntimeException("Retries exhausted");
                }))
                .onErrorResume(Exception.class, e -> {
                    System.out.println("An error occurred: " + e.getMessage());
                    return Mono.empty();})
                .subscribe(content -> {
                    try (FileWriter fileWriter = new FileWriter("mediaTitlesAndReleaseDates.txt", false)) {
                        fileWriter.write(content);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
    
    //2 Total count of media items
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
                .onErrorResume(Exception.class, e -> {
                    System.out.println("An error occurred: " + e.getMessage());
                    return Mono.empty();})
                .subscribe(count -> {
                    try (FileWriter fileWriter = new FileWriter("countMediaItems.txt", false)) {
                        fileWriter.write("Total count of media items: " + count + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
    
    //3 Total count of media items that are really good (rating >8)
    private static void writeMediaItemsWithHighRatings(WebClient webClient) {
        webClient.get().uri("/media")
                .retrieve()
                .bodyToFlux(Media.class)
                .filter(media -> media.getAverageRating() > 8)
                .reduce(0L, (count, media) -> count + 1)
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .onErrorResume(Exception.class, e -> {
                    System.out.println("An error occurred: " + e.getMessage());
                    return Mono.empty();})
                .subscribe(count -> {
                    try (FileWriter fileWriter = new FileWriter("highRatedMediaItems.txt", false)) {
                        fileWriter.write("Total count of media items with rating > 8: " + count + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    //4 Total count of media that is subscribed
    private static void writeMediaThatIsSubscribed(WebClient webClient) {

        Set<Long> uniqueSet = new HashSet<>();
   
        webClient.get().uri("/user-media")
                .retrieve()
                .bodyToFlux(UserMedia.class)
                .filter(data -> uniqueSet.add(data.getMediaId()))
                .count()
                .subscribe(count -> {
                    try (FileWriter fileWriter = new FileWriter("4.txt", false)) {
                        fileWriter.write("Total count of media that is subscribed: " + count);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    //5 Total count of media that are from the the 80's 
    private static void writeMediaFromTheEighties(WebClient webClient) {
        webClient.get().uri("/media")
                .retrieve()
                .bodyToFlux(Media.class)
                .filter(media -> media.getReleaseDate().isAfter(LocalDate.of(1980, 1, 1)) &&
                                 media.getReleaseDate().isBefore(LocalDate.of(1989, 12, 31)))
                .sort((m1, m2) -> Double.compare(m2.getAverageRating(), m1.getAverageRating()))
                .reduce("", (acc, media) -> acc + "Title: " + media.getTitle() + ", Rating: " + media.getAverageRating() + "\n")
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .onErrorResume(Exception.class, e -> {
                    System.out.println("An error occurred: " + e.getMessage());
                    return Mono.empty();})
                .subscribe(content -> {
                    try (FileWriter fileWriter = new FileWriter("mediaFromTheEighties.txt", false)) {
                        fileWriter.write(content);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
    // 6. Average and Standard deviations of all media items ratings
    private static void writeAverageAndStandardDeviationOfMediaRatings(WebClient webClient) {
        webClient.get().uri("/media")
                .retrieve()
                .bodyToFlux(Media.class)
                .map(Media::getAverageRating)
                .reduce(new double[]{0.0, 0.0, 0.0}, (acc, rating) -> {
                    acc[0] += rating;  // Sum of ratings
                    acc[1] += 1;       // Count of ratings
                    acc[2] += Math.pow(rating, 2); // Sum of squared ratings for variance calculation
                    return acc;
                })
                .map(acc -> {
                    double average = acc[0] / acc[1];  // Calculate average
                    double variance = (acc[2] / acc[1]) - Math.pow(average, 2);  // Calculate variance
                    double standardDeviation = Math.sqrt(variance);  // Calculate standard deviation
                    return new double[]{average, standardDeviation};
                })
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .onErrorResume(Exception.class, e -> {
                    System.out.println("An error occurred: " + e.getMessage());
                    return Mono.empty();
                })
                .subscribe(stats -> {
                    try (FileWriter fileWriter = new FileWriter("mediaRatingsStats.txt", false)) {
                        fileWriter.write("Average rating: " + stats[0] + "\n");
                        fileWriter.write("Standard deviation: " + stats[1] + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    
    //7 Name of the oldest media item
    private static void writeOldestMediaItemName(WebClient webClient) {
        webClient.get().uri("/media")
                .retrieve()
                .bodyToFlux(Media.class)
                .reduce((oldest, media) -> media.getReleaseDate().isBefore(oldest.getReleaseDate()) ? media : oldest)
                .map(Media::getTitle)
                .switchIfEmpty(Mono.just("No media items available"))
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .onErrorResume(Exception.class, e -> {
                    System.out.println("An error occurred: " + e.getMessage());
                    return Mono.empty();})
                .subscribe(title -> {
                    try (FileWriter fileWriter = new FileWriter("oldestMediaItem.txt", false)) {
                        fileWriter.write("Oldest media item: " + title + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    //8 Average number of users per media item
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
                .onErrorResume(Exception.class, e -> {
                    System.out.println("An error occurred: " + e.getMessage());
                    return Mono.empty();})
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
    
    //9 Name and number of users per media item, sorted by age of descending number
    private static void writeUserDataWithSubscribedMedia(WebClient webClient) {
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
                                (user, media) -> Map.entry(media.getTitle(), user)
                            )
                )
                .groupBy(Map.Entry::getKey)  
                .flatMap(mediaGroup -> mediaGroup.collectList())  
                .map(usersForMedia -> {
                    usersForMedia.sort((entry1, entry2) -> Integer.compare(entry2.getValue().getAge(), entry1.getValue().getAge()));
    
                    String mediaTitle = usersForMedia.get(0).getKey(); 
                    int userCount = usersForMedia.size();
                    String userNames = usersForMedia.stream()
                            .map(entry -> entry.getValue().getName()) 
                            .collect(Collectors.joining(", "));
                    return "Media: " + mediaTitle + " | Users (" + userCount + "): " + userNames;
                })
                .reduce((result1, result2) -> result1 + "\n" + result2)  
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .onErrorResume(Exception.class, e -> {
                    System.out.println("An error occurred: " + e.getMessage());
                    return Mono.empty();
                })
                .subscribe(userData -> {
                    try (FileWriter fileWriter = new FileWriter("userSubscribedMedia.txt", false)) {
                        fileWriter.write(userData + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
    

    // 10 Complete data of all users, by adding the names of subscribed media items
    private static void writeCompleteUserDataWithSubscribedMedia(WebClient webClient) {
        try (FileWriter fileWriter = new FileWriter("completeUserData.txt", false)) {
            fileWriter.write(""); 
        } catch (IOException e) {
            e.printStackTrace();
        }

        webClient.get().uri("/users")
                .retrieve()
                .bodyToFlux(User.class)
                .flatMap(user ->
                    webClient.get().uri("/user-media?userId=" + user.getId())
                            .retrieve()
                            .bodyToFlux(UserMedia.class)
                            .flatMap(userMedia ->
                                webClient.get().uri("/media/{id}", userMedia.getMediaId())
                                        .retrieve()
                                        .bodyToMono(Media.class)
                                        .map(Media::getTitle)
                            )
                            .reduce((title1, title2) -> title1 + ", " + title2)
                            .map(mediaTitles -> {
                                return "User: " + user.getName() + " | Age: " + user.getAge() +
                                        " | Gender: " + user.getGender() +
                                        " | Subscribed Media: " + (mediaTitles == null ? "None" : mediaTitles);
                            })
                )
                .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(2)))
                .onErrorResume(Exception.class, e -> {
                    System.out.println("An error occurred: " + e.getMessage());
                    return Mono.empty();
                })
                .subscribe(userData -> {
                    try (FileWriter fileWriter = new FileWriter("completeUserData.txt", true)) { 
                        fileWriter.write(userData + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

        

}
