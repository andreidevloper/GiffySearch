package com.as.giffysearch.ThirdParty.Glide;

/*
 * Created by Andrejs Skorinko on 12/1/2017.
 */

import android.content.Context;

import com.as.giffysearch.Models.JSON.Giphy.GifResult;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

import java.io.InputStream;

// Look Getting Started (Java) at http://bumptech.github.io/glide/doc/generatedapi.html
// https://futurestud.io/tutorials/glide-customize-glide-with-modules
@GlideModule
public final class GiffyGlideModuleApp extends AppGlideModule
{
    @Override
    public void applyOptions(Context context, GlideBuilder builder)
    {

    }

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry)
    {
        registry.append(GifResult.class, InputStream.class, new GiphyModelLoader.Factory());
    }
}

