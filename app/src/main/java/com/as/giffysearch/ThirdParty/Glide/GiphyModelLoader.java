package com.as.giffysearch.ThirdParty.Glide;

import android.text.TextUtils;

import com.as.giffysearch.Models.JSON.Giphy.GifImage;
import com.as.giffysearch.Models.JSON.Giphy.GifResult;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;

import java.io.InputStream;

public final class GiphyModelLoader extends BaseGlideUrlLoader<GifResult>
{

  private GiphyModelLoader(ModelLoader<GlideUrl, InputStream> urlLoader)
  {
    super(urlLoader);
  }

    @Override
    protected String getUrl(GifResult gifResult, int width, int height, Options options)
    {
        GifImage fixedHeight = gifResult.getImages().getFixedHeight();
        int fixedHeightDifference = getDifference(fixedHeight, width, height);

        GifImage fixedWidth = gifResult.getImages().getFixedWidth();
        int fixedWidthDifference = getDifference(fixedWidth, width, height);

        String fixedHeightUrl = fixedHeight.getUrl();
        if (fixedHeightDifference < fixedWidthDifference && !TextUtils.isEmpty(fixedHeightUrl))
        {
            return fixedHeightUrl;
        }

        String fixedWidthUrl = fixedWidth.getUrl();
        if (!TextUtils.isEmpty(fixedWidthUrl))
        {
            return fixedWidthUrl;
        }

        String originalUrl = gifResult.getImages().getOriginal().getUrl();
        if (!TextUtils.isEmpty(originalUrl))
        {
            return originalUrl;
        }
        else
        {
            return null;
        }
    }

    private static int getDifference(GifImage gifImage, int width, int height)
    {
        return Math.abs(width - gifImage.getWidth()) + Math.abs(height - gifImage.getHeight());
    }

    @Override
    public boolean handles(GifResult gifResult)
    {
        return true;
    }

    public static final class Factory implements ModelLoaderFactory<GifResult, InputStream>
  {
    @Override
    public ModelLoader<GifResult, InputStream> build(MultiModelLoaderFactory multiFactory)
    {
      return new GiphyModelLoader(multiFactory.build(GlideUrl.class, InputStream.class));
    }

    @Override
    public void teardown()
    {
      // Do nothing.
    }
  }
}
