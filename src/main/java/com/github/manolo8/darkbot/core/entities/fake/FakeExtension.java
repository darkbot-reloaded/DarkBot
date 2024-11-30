package com.github.manolo8.darkbot.core.entities.fake;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.objects.LocationInfo;
import eu.darkbot.api.game.entities.FakeEntity;
import eu.darkbot.api.game.other.Location;
import eu.darkbot.util.Timer;

public interface FakeExtension extends FakeEntity {

  FakeExtension.Data getFakeData();

  @Override
  default void setRemoveOnSelect(boolean remove) {
    getFakeData().removeOnSelect = remove;
  }

  @Override
  default void setRemoveDistance(long removeDistance) {
    getFakeData().removeDistance = removeDistance;
  }

  @Override
  default void setLocation(Location location) {
    ((LocationInfo) getLocationInfo()).updatePosition(location.x(), location.y());
  }

  @Override
  default void setTimeout(long keepAlive) {
    getFakeData().setTimeout(keepAlive);
  }

  class Data {
    private final Entity entity;
    private Timer timeout;
    private long removeDistance;
    private boolean removeOnSelect;

    public Data(Entity entity) {
      this.entity = entity;
      this.entity.main = Main.INSTANCE;
      this.entity.removed = false;
    }

    private void setTimeout(long keepAlive) {
      if (keepAlive != -1) {
        timeout = Timer.get(keepAlive);
        timeout.activate();
      }
      else timeout = null;
    }

    public boolean isInvalid() {
      if (timeout != null && timeout.isInactive()) return true;
      return removeDistance == -1 || entity.main.hero.distanceTo(entity) < removeDistance;
    }

    public boolean trySelect(boolean tryAttack) {
      if (removeOnSelect) entity.removed();
      return false;
    }
  }

}
