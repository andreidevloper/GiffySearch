package com.as.giffysearch.Utility;

import android.util.Log;

/**
 * Created by Andrejs Skorinko on 11/29/2017.
 *
 */

public class Debugging
{
    public static class Profiler
    {
        private long startTime_;
        private long endTime_;
        private long diffTime_;

        private long totalDiffTime_;
        private long counter_;

        public void startTimer()
        {
            startTime_ = System.nanoTime();
        }

        public void stopTimer()
        {
            endTime_ = System.nanoTime();
            diffTime_ = endTime_ - startTime_;

            totalDiffTime_ += diffTime_;
            counter_++;
        }

        // in milliseconds
        public long getDiffTime()
        {
            return (diffTime_ / 1000000);
        }

        // in milliseconds
        private long getAverageDiffTime() { if(counter_ != 0) return ((totalDiffTime_ / 1000000) / counter_); else return -1; }

        public void logDiffTime(String logTag)
        {
            Debugging.logClass(Log.INFO, logTag, "Elapsed miliseconds: " + ((diffTime_ /1000000) + " ms."));
        }

        public void logAverageDiffTime(String logTag)
        {
            Debugging.logClass(Log.INFO, logTag, "Average diff time: " + getAverageDiffTime() + " ms. " +
                                                          " For " + counter_ + " requests.");
        }
    }

    public static void logClassThrowable(int level, String logTag, String outputText, Throwable exception)
    {
        switch (level)
        {
            case Log.DEBUG:
            {
                Log.d(logTag, outputText, exception);
                break;
            }

            case Log.INFO:
            {
                Log.i(logTag, outputText, exception);
                break;
            }
            case Log.WARN:
            {
                Log.w(logTag, outputText, exception);
                break;
            }

            case Log.ERROR:
            {
                Log.e(logTag, outputText, exception);
                break;
            }

            case Log.VERBOSE:
            {
                Log.v(logTag, outputText, exception);
                break;
            }

            default:
            {
                Log.v(logTag, outputText, exception);
                break;
            }
        }
    }

    public static void logClass(int level, String logTag, String outputText)
    {
        switch (level)
        {
            case Log.DEBUG:
            {
                Log.d(logTag, outputText);
                break;
            }

            case Log.INFO:
            {
                Log.i(logTag, outputText);
                break;
            }
            case Log.WARN:
            {
                Log.w(logTag, outputText);
                break;
            }

            case Log.ERROR:
            {
                Log.e(logTag, outputText);
                break;
            }

            case Log.VERBOSE:
            {
                Log.v(logTag, outputText);
                break;
            }

            default:
            {
                Log.v(logTag, outputText);
                break;
            }
        }
    }
}

