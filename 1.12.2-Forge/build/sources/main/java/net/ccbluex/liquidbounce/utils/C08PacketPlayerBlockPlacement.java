package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.api.minecraft.util.WBlockPos;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;

public class C08PacketPlayerBlockPlacement extends CPacketPlayerTryUseItem implements Packet<INetHandlerPlayServer> {
    private static final BlockPos field_179726_a = new BlockPos(-1, -1, -1);
    private BlockPos position;
    private WBlockPos position2;
    private int placedBlockDirection;
    private EnumHand stack;
    private ItemStack stack2;
    public float facingX;
    public float facingY;
    public float facingZ;

    public C08PacketPlayerBlockPlacement() {
    }

    public C08PacketPlayerBlockPlacement(EnumHand p_i45930_1_) {
        this(field_179726_a, 255, p_i45930_1_, 0.0F, 0.0F, 0.0F);
    }

    public C08PacketPlayerBlockPlacement(BlockPos p_i45931_1_, int p_i45931_2_, ItemStack p_i45931_3_, float p_i45931_4_, float p_i45931_5_, float p_i45931_6_) {
        this.position = p_i45931_1_;
        this.placedBlockDirection = p_i45931_2_;
        this.stack2 = p_i45931_3_ != null ? p_i45931_3_.copy() : null;
        this.facingX = p_i45931_4_;
        this.facingY = p_i45931_5_;
        this.facingZ = p_i45931_6_;
    }

    public C08PacketPlayerBlockPlacement(WBlockPos p_i45931_1_, int p_i45931_2_, EnumHand p_i46858_3_, float p_i45931_4_, float p_i45931_5_, float p_i45931_6_) {
        this.position2 = p_i45931_1_;
        this.placedBlockDirection = p_i45931_2_;
        this.stack = p_i46858_3_;
        this.facingX = p_i45931_4_;
        this.facingY = p_i45931_5_;
        this.facingZ = p_i45931_6_;
    }

    public C08PacketPlayerBlockPlacement(BlockPos p_i45931_1_, int p_i45931_2_, EnumHand p_i46858_3_, float p_i45931_4_, float p_i45931_5_, float p_i45931_6_) {
        this.position = p_i45931_1_;
        this.placedBlockDirection = p_i45931_2_;
        this.stack = p_i46858_3_;
        this.facingX = p_i45931_4_;
        this.facingY = p_i45931_5_;
        this.facingZ = p_i45931_6_;
    }

    public void readPacketData(PacketBuffer p_readPacketData_1_) throws IOException {
        this.position = p_readPacketData_1_.readBlockPos();
        this.placedBlockDirection = p_readPacketData_1_.readUnsignedByte();
        this.stack = p_readPacketData_1_.readEnumValue(EnumHand.class);
        this.facingX = (float)p_readPacketData_1_.readUnsignedByte() / 16.0F;
        this.facingY = (float)p_readPacketData_1_.readUnsignedByte() / 16.0F;
        this.facingZ = (float)p_readPacketData_1_.readUnsignedByte() / 16.0F;
    }

    public void writePacketData(PacketBuffer p_writePacketData_1_) throws IOException {
        p_writePacketData_1_.writeBlockPos(this.position);
        p_writePacketData_1_.writeByte(this.placedBlockDirection);
        p_writePacketData_1_.writeEnumValue(this.stack);
        p_writePacketData_1_.writeByte((int)(this.facingX * 16.0F));
        p_writePacketData_1_.writeByte((int)(this.facingY * 16.0F));
        p_writePacketData_1_.writeByte((int)(this.facingZ * 16.0F));
    }

    public void processPacket(INetHandlerPlayServer p_processPacket_1_) {
        p_processPacket_1_.processTryUseItem(this);
    }

    public BlockPos getPosition() {
        return this.position;
    }

    public int getPlacedBlockDirection() {
        return this.placedBlockDirection;
    }

    public EnumHand getStack() {
        return this.stack;
    }

    public float getPlacedBlockOffsetX() {
        return this.facingX;
    }

    public float getPlacedBlockOffsetY() {
        return this.facingY;
    }

    public float getPlacedBlockOffsetZ() {
        return this.facingZ;
    }
}
