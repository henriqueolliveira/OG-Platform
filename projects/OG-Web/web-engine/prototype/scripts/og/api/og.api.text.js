/*
 * @copyright 2009 - 2011 by OpenGamma Inc
 * @license See distribution for license
 *
 * API call for making and caching static requests
 */
$.register_module({
    name: 'og.api.text',
    dependencies: ['og.api.common'],
    obj: function () {
        var html_cache = {}, module = this, html_root = module.htmlRoot,
            start_loading = og.api.common.start_loading, end_loading = og.api.common.end_loading;
        /**
         * takes a module name (like <code>'og.common.details.foo'</code>) and returns a path:
         * <code>app_root/modules/common/details/og.common.details.foo.jsp</code>
         * not public, but documented because <code>og.api.text</code> uses it
         * @see og.api.text
         * @inner
         */
        var module_path = function (page) {
            return html_root + [page.split('.').slice(1, -1).join('/'), page.toLowerCase() + '.html'].join('/');
        };
        return function (config) {
            if (typeof config === 'undefined') throw new TypeError('static: config is undefined');
            if (typeof config.url !== 'string' && typeof config.module !== 'string')
                throw new TypeError('static: either config.url or config.module must be a string');
            var url = config.url || module_path(config.module),
                do_not_cache = config.do_not_cache, clear_cache = config.clear_cache,
                handler = function (html) {
                    if (!do_not_cache) html_cache[url] = html;
                    end_loading();
                    config.handler(html);
                };
            start_loading(config.loading);
            if (clear_cache) delete html_cache[url];
            if (url in html_cache) return (do_not_cache = true) && handler(html_cache[url]);
            $.ajax({url: url, success: handler});
        };
    }
});