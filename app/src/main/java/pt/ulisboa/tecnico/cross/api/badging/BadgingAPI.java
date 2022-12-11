package pt.ulisboa.tecnico.cross.api.badging;

import java.io.IOException;
import java.util.Set;

import pt.ulisboa.tecnico.cross.api.CallWrapper;
import pt.ulisboa.tecnico.cross.contract.gamification.Badging;
import pt.ulisboa.tecnico.cross.model.badging.Badge;
import retrofit2.Retrofit;

public class BadgingAPI {

  private final BadgingService badgingService;

  public BadgingAPI(Retrofit retrofit) {
    this.badgingService = retrofit.create(BadgingService.class);
  }

  public void getBadges(Set<Badge> outputBadges) throws IOException {
    new CallWrapper<>(badgingService.getBadges())
        .execute().getBadgesList().stream().map(this::fromProtobuf).forEach(outputBadges::add);
  }

  private Badge fromProtobuf(Badging.Badge badge) {
    return new Badge(
        badge.getId(),
        badge.getPosition(),
        badge.getImageURL(),
        badge.getMainLocale(),
        badge.getNamesMap(),
        badge.getQuestsMap(),
        badge.getAchievementsMap());
  }
}
