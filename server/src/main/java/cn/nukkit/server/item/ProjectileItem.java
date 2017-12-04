package cn.nukkit.server.item;

import cn.nukkit.server.Player;
import cn.nukkit.server.entity.Entity;
import cn.nukkit.server.entity.projectile.EntityProjectile;
import cn.nukkit.server.event.entity.ProjectileLaunchEvent;
import cn.nukkit.server.level.sound.LaunchSound;
import cn.nukkit.server.math.Vector3;
import cn.nukkit.server.nbt.tag.CompoundTag;
import cn.nukkit.server.nbt.tag.DoubleTag;
import cn.nukkit.server.nbt.tag.FloatTag;
import cn.nukkit.server.nbt.tag.ListTag;

/**
 * @author CreeperFace
 */
public abstract class ProjectileItem extends Item {

    public ProjectileItem(int id, Integer meta, int count, String name) {
        super(id, meta, count, name);
    }

    abstract public String getProjectileEntityType();

    abstract public float getThrowForce();

    public boolean onClickAir(Player player, Vector3 directionVector) {
        CompoundTag nbt = new CompoundTag()
                .putList(new ListTag<DoubleTag>("Pos")
                        .add(new DoubleTag("", player.x))
                        .add(new DoubleTag("", player.y + player.getEyeHeight()))
                        .add(new DoubleTag("", player.z)))
                .putList(new ListTag<DoubleTag>("Motion")
                        .add(new DoubleTag("", directionVector.x))
                        .add(new DoubleTag("", directionVector.y))
                        .add(new DoubleTag("", directionVector.z)))
                .putList(new ListTag<FloatTag>("Rotation")
                        .add(new FloatTag("", (float) player.yaw))
                        .add(new FloatTag("", (float) player.pitch)));

        this.correctNBT(nbt);

        Entity projectile = Entity.createEntity(this.getProjectileEntityType(), player.getLevel().getChunk(player.getFloorX() >> 4, player.getFloorZ() >> 4), nbt, player);
        if (projectile != null) {
            projectile.setMotion(projectile.getMotion().multiply(this.getThrowForce()));
            this.count--;

            if (projectile instanceof EntityProjectile) {
                ProjectileLaunchEvent ev = new ProjectileLaunchEvent((EntityProjectile) projectile);

                player.getServer().getPluginManager().callEvent(ev);
                if (ev.isCancelled()) {
                    projectile.kill();
                } else {
                    projectile.spawnToAll();
                    player.getLevel().addSound(new LaunchSound(player), player.getViewers().values());
                }
            } else {
                projectile.spawnToAll();
            }
        } else {
            return false;
        }
        return true;
    }

    protected void correctNBT(CompoundTag nbt) {

    }
}