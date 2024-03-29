package frc.robot.MPU6050;

import static frc.robot.MPU6050.MPU6050Constants.AutoGenerated.MPU6050_DEFAULT_ADDRESS;
import static frc.robot.MPU6050.MPU6050Constants.AutoGenerated.MPU6050_DMP_MEMORY_BANK_SIZE;
import static frc.robot.MPU6050.MPU6050Constants.AutoGenerated.MPU6050_RA_BANK_SEL;
import static frc.robot.MPU6050.MPU6050Constants.AutoGenerated.MPU6050_RA_MEM_R_W;
import static frc.robot.MPU6050.MPU6050Constants.AutoGenerated.MPU6050_RA_MEM_START_ADDR;
import static frc.robot.MPU6050.MPU6050Constants.AutoGenerated.MPU6050_RA_WHO_AM_I;
import static frc.robot.MPU6050.MPU6050Constants.AutoGenerated.MPU6050_DMP_MEMORY_CHUNK_SIZE;

import java.util.Arrays;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.I2C;


public abstract class MPU6050Base{
    BetterI2C mpu6050;
    
    public MPU6050Base(I2C.Port port) {
        this(port, MPU6050_DEFAULT_ADDRESS);
    }
    
    public MPU6050Base(I2C.Port port, int address) {
        mpu6050 = new BetterI2C(port, address);
        if (!isConnected()) {
            DriverStation.reportError("MPU6050 NOT CONNECTED!! PLEASE RESTART CODE AFTER CONNECTION! CANNOT INITILAZE MPU6050 ",false);
            return;
        }
        initialize();
    }


    public boolean isConnected() {
        return mpu6050.readBytes(MPU6050_RA_WHO_AM_I, 1)[0] == 0x68;
    }

    long map(long x, long in_min, long in_max, long out_min, long out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    /**
     * Write to the DMP memory.
     * This function prevents I2C writes past the bank boundaries. The DMP memory
     * is only accessible when the chip is awake.
     * @param mem_addr Memory location (bank << 8 | start address)
     * @param length Number of bytes to write.
     * @param data Bytes to write to memory.
     * @return Transfer Aborted... false for success, true for aborted.
    */
    protected boolean writeMem(short mem_addr, short length, char[] data) {
        byte[] tmp = new byte[2];
        byte[] tmp2 = new byte[data.length];
        
        for (int i = 0; i < data.length; i++) {
            tmp2[i] = (byte) data[i];
        }

        tmp[0] = (byte) (mem_addr >> 8);
        tmp[1] = (byte) (mem_addr & 0xFF);

        // Check bank boundaries
        if (tmp[1] + length > MPU6050_DMP_MEMORY_BANK_SIZE) {
            return true;
        }
        
        // After this point, I couldn't find any documentation on what this code does, so I'm just going to assume it works.
        // And I had to translate it from C++ to Java, so it might not work.
        // Especially the write Functions as in the source code the write function had a count but here there is none... 
        if (mpu6050.writeBytes(MPU6050_RA_BANK_SEL, tmp, 2)) return true;

        if (mpu6050.writeBytes(MPU6050_RA_MEM_R_W,  tmp2)) return true;
        
        return false;
    }
    
    /**
     * Read from the DMP memory.
     * @param mem_addr Memory location (bank << 8 | start address)
     * @param length Number of bytes to read.
     * @return Data read from memory Null if aborted.
     */
    protected byte[] readMem(short mem_addr, short length) {
        byte[] tmp = new byte[2];
        byte[] buffer = new byte[length];
        
        tmp[0] = (byte) (mem_addr >> 8);
        tmp[1] = (byte) (mem_addr & 0xFF);

        if (tmp[1] + length > MPU6050_DMP_MEMORY_BANK_SIZE) {
            return null;
        }

        if (mpu6050.writeBytes(MPU6050_RA_BANK_SEL, tmp)) return null;

        buffer = mpu6050.readBytes(MPU6050_RA_MEM_R_W, length);

        return buffer;
    }

    protected boolean writeProgMemoryBlock(char[] data, int dataSize, int bank, int adress, boolean verify) {
        return writeMemoryBlock(data, dataSize, bank, adress, verify, true);
    }

    protected boolean writeMemoryBlock(char[] data, int dataSize, int bank, int address, boolean verify, boolean useProgMem) {
        setMemoryBank(bank);
        setMemoryStartAddress(address);
        int chunkSize;
        char[] progBuffer = new char[MPU6050_DMP_MEMORY_CHUNK_SIZE];
        byte[] verifyBuffer = new byte[MPU6050_DMP_MEMORY_CHUNK_SIZE];
        byte[] progBufferTmp = new byte[MPU6050_DMP_MEMORY_CHUNK_SIZE];
        int i;
        int j;

        for (i = 0; i<dataSize;) {
            chunkSize = MPU6050_DMP_MEMORY_CHUNK_SIZE;

            if (i + chunkSize > dataSize) chunkSize = dataSize - i;    // make sure we don't go past the data size
            
            if (chunkSize > 256 - address) chunkSize = 256 - address;  // make sure this chunk doesn't go past the bank boundary (256 bytes)

            if (useProgMem) {
                // write the chunk of data as specified
                if (chunkSize < MPU6050_DMP_MEMORY_CHUNK_SIZE) {progBuffer = new char[chunkSize]; progBufferTmp = new byte[chunkSize];} // weird quirk of the write function, we need to send a large enough block, here we assume 16 is enough (default
                for (j = 0; j < chunkSize; j++) progBuffer[j] = data[i + j];
            } else {
                // write the chunk of data as specified
                progBuffer = Arrays.copyOfRange(data, i, data.length);
            }
            
            for (j = 0; j < chunkSize; j++) progBufferTmp[j] = (byte) progBuffer[j];

            mpu6050.writeBytes(MPU6050_RA_MEM_R_W, progBufferTmp);

            if (verify && verifyBuffer != null) { // verify data if needed
                setMemoryBank(bank);
                setMemoryStartAddress(address);
                verifyBuffer = mpu6050.readBytes(MPU6050_RA_MEM_R_W, chunkSize);
                
                if (!Arrays.equals(progBufferTmp, verifyBuffer)) { // uh oh! we fucked up.
                    System.out.println("Block write verification error, bank " + bank + ", address " + address + ", At " + i + " of " + dataSize);
                    return true;
                }
            }
            // increase byte index by [chunkSize]
            i += chunkSize;

            address += chunkSize; // does not wrap around to 0 automatically. 
            if (address >= 256) address=0; // Wrap around to the next block, if necessary.
            
            if (i < dataSize) {
                if (address == 0) bank++;
                setMemoryBank(bank);
                setMemoryStartAddress(address);
            }
        }
        return false;
    }
    

    protected void setMemoryBank(int bank, boolean prefetchEnabled, boolean userBank)  {
        bank &= 0x1F;
        if (userBank) bank |= 0x20;
        if (prefetchEnabled) bank |= 0x40;
        mpu6050.write(MPU6050_RA_BANK_SEL, bank);
    }

    protected void setMemoryBank(int bank) {
        setMemoryBank(bank, false, false);
    }

    protected void setMemoryStartAddress(int address) {
        mpu6050.write(MPU6050_RA_MEM_START_ADDR, address);
    }

    /**
     * Initilizes the MPU6050.
     * Will be called in the constructor.
     */
    abstract void initialize();
}
