import play.api.mvc.EssentialAction;
import play.api.mvc.EssentialFilter;
import play.filters.cors.CORSFilter;
import play.http.HttpFilters;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.Executor;

@Singleton
class SecurityFilter implements EssentialFilter {

    private final Executor executor;

    @Inject
    public SecurityFilter(Executor executor) {
        super();
        this.executor = executor;
    }

    @Override
    public EssentialAction apply(EssentialAction next) {
        System.out.println("Hey");
        return next;
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
        System.out.println("Hello filter");
        return new CORSFilter[]{corsFilter};
    }
}