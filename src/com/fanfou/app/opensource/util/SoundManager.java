/*******************************************************************************
 * Copyright 2011, 2012, 2013 fanfou.com, Xiaoke, Zhang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.fanfou.app.opensource.util;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.util.SparseIntArray;

import com.fanfou.app.opensource.R;

/**
 * @author mcxiaoke
 * @version 1.0 2011.11.16
 * @version 1.1 2011.12.09
 * @version 1.2 2011.12.26
 * @see http
 *      ://www.droidnova.com/creating-sound-effects-in-android-part-2,695.html
 * 
 */
public final class SoundManager {
    private static SoundManager _instance;
    private static SoundPool mSoundPool;
    private static SparseIntArray mSoundPoolMap;
    private static AudioManager mAudioManager;
    private static Activity mContext;

    /**
     * Add a new Sound to the SoundPool
     * 
     * @param Index
     *            - The Sound Index for Retrieval
     * @param SoundID
     *            - The Android ID for the Sound asset.
     */
    public static void addSound(final int Index, final int SoundID) {
        SoundManager.mSoundPoolMap
                .put(Index, SoundManager.mSoundPool.load(SoundManager.mContext,
                        SoundID, 1));
    }

    /**
     * Deallocates the resources and Instance of SoundManager
     */
    public static void cleanup() {
        if (SoundManager.mSoundPool != null) {
            SoundManager.mSoundPool.release();
            SoundManager.mSoundPool = null;
        }
        if (SoundManager.mSoundPoolMap != null) {
            SoundManager.mSoundPoolMap.clear();
        }
        SoundManager._instance = null;
    }

    /**
     * Requests the instance of the Sound Manager and creates it if it does not
     * exist.
     * 
     * @return Returns the single instance of the SoundManager
     */
    public static synchronized SoundManager getInstance() {
        if (SoundManager._instance == null) {
            SoundManager._instance = new SoundManager();
        }
        return SoundManager._instance;
    }

    /**
     * Initialises the storage for the sounds
     * 
     * @param theContext
     *            The Application context
     */
    public static void initSounds(final Activity context) {
        SoundManager.mContext = context;
        SoundManager.mAudioManager = (AudioManager) SoundManager.mContext
                .getSystemService(Context.AUDIO_SERVICE);
        SoundManager.mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
    }

    /**
     * Loads the various sound assets Currently hardcoded but could easily be
     * changed to be flexible.
     */
    public static void loadSounds() {
        SoundManager.mSoundPoolMap.put(1, SoundManager.mSoundPool.load(
                SoundManager.mContext, R.raw.pop, 1));
    }

    /**
     * Plays a Sound
     * 
     * @param index
     *            - The Index of the Sound to be played
     * @param speed
     *            - The Speed to play not, not currently used but included for
     *            compatibility
     */
    public static void playSound(final int index, final float speed) {
        float streamVolume = SoundManager.mAudioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        streamVolume = streamVolume
                / SoundManager.mAudioManager
                        .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        try {
            SoundManager.mSoundPool.play(SoundManager.mSoundPoolMap.get(index),
                    streamVolume, streamVolume, 1, 0, speed);
        } catch (final RuntimeException e) {
            Log.e("SoundManager",
                    "playSound: index " + index + " error:" + e.getMessage());
        }
    }

    /**
     * Stop a Sound
     * 
     * @param index
     *            - index of the sound to be stopped
     */
    public static void stopSound(final int index) {
        SoundManager.mSoundPool.stop(SoundManager.mSoundPoolMap.get(index));
    }

    private SoundManager() {
        SoundManager.mSoundPoolMap = new SparseIntArray();
    }
}
