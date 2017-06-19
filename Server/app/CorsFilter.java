import play.api.mvc.EssentialFilter;
import play.filters.cors.CORSFilter;
import play.http.HttpFilters;

import javax.inject.Inject;
import javax.inject.Singleton;

public class CorsFilter implements HttpFilters {
    @Inject
    public CorsFilter(CORSFilter corsFilter) {
    }

    @Override
    public EssentialFilter[] filters() {
        return new EssentialFilter[0];
    }
}


@Singleton
class Filters implements HttpFilters {
    private final CORSFilter corsFilter;

    @Inject()
    Filters(CORSFilter corsFilter) {
        this.corsFilter = corsFilter;
    }

    public EssentialFilter[] filters() {
        return new CORSFilter[]{corsFilter};
    }
}