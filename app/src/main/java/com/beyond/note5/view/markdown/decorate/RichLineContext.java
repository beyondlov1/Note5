package com.beyond.note5.view.markdown.decorate;

import com.beyond.note5.view.markdown.decorate.bean.RichLine;
import com.beyond.note5.view.markdown.decorate.plain.RichLinePlainer;
import com.beyond.note5.view.markdown.decorate.resolver.RichLineResolver;

public class RichLineContext{

        private RichLineResolver resolver;
        private RichLinePlainer plainer;
        private String tag;
        private Class spanClass;

        public RichLineContext(String tag, Class spanClass,RichLineResolver resolver, RichLinePlainer plainer) {
            this.resolver = resolver;
            this.plainer = plainer;
            this.tag = tag;
            this.spanClass = spanClass;

            this.resolver.setContext(this);
            this.plainer.setContext(this);
        }

        public void resolve(RichLine richLine){
            resolver.resolve(richLine);
        }

        public String plain(RichLine richLine){
            return plainer.plain(richLine);
        }

        public boolean supportResolve(RichLine richLine) {
            return resolver.supportResolve(richLine);
        }

        public boolean supportPlain(RichLine richLine) {
            return plainer.supportPlain(richLine);
        }

        public RichLineResolver getResolver() {
            return resolver;
        }

        public void setResolver(RichLineResolver resolver) {
            this.resolver = resolver;
        }

        public RichLinePlainer getPlainer() {
            return plainer;
        }

        public void setPlainer(RichLinePlainer plainer) {
            this.plainer = plainer;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public Class getSpanClass() {
            return spanClass;
        }

        public void setSpanClass(Class spanClass) {
            this.spanClass = spanClass;
        }
    }