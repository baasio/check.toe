
package io.baas.checktoe.ui;

import com.nostra13.universalimageloader.core.ImageLoader;

import io.baas.checktoe.ui.main.SearchableFragmentInterface;

public abstract class SearchableBaseFragment extends BaseFragment implements
        SearchableFragmentInterface {
    protected ImageLoader mImageLoader = ImageLoader.getInstance();
}
