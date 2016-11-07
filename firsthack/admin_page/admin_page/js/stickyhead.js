/*
* Sticky Header 1.1
*
* Copyright (c) 2012 Anurag Uniyal
*
*/
(function( $ ) {
    $.fn.stickyhead = function(method) {
    
        return this.each(function(){
                if(typeof method === 'string'){
                    get_stickyhead($(this)).call_method(method)
                }else{
                    get_stickyhead($(this), method)
                }
            })
    };
    
    function get_stickyhead(table, options){
        //check if we already created a stickyhead
        var stickyhead = table.data('stickyhead')
        if(stickyhead===undefined){
            stickyhead = new Stickyhead(table, options)
            stickyhead.init()
            table.data('stickyhead', stickyhead)
        }
        return stickyhead
    }
  
    function Stickyhead(table, options){
        var settings = $.extend(
        {
            'top_bar':null, 
            'top_bar_height':null,
            'with_data_and_events':true,
            'css': {'background-color':'#ffffff', 'border-bottom':'1px solid #cccccc'}
        },
        options);
        
        var _this = this;
        
        //various elements used
        var thepage = $(window);//document is not working for scroll on IE8
        var top_bar = $(settings.top_bar);
        var the_table = table;
        var sticky_table = null;
        var thead = null;        
        var orig_th_list = null;
        var clone_th_list = null;
        
        // variables
        var scroll_start = true; 
        var scroll_start_delay = 1000;
        var last_update_time = null;
        var visible = false;
        
        // public methods called via call_method
        var methods = {}
        
        this.init = function(){
            sticky_table = the_table.clone(settings.with_data_and_events);
            sticky_table.find('tbody').remove()
            sticky_table.css({'position':'fixed'}).css(settings.css).hide()
            sticky_table.appendTo('body')
            thead = sticky_table.find('thead')
            orig_th_list = the_table.find('th')
            clone_th_list = sticky_table.find('th')
            
            this.update(true)
        }
        
        methods['update'] = function(resize){
            var now = new Date();
            if(last_update_time==null || now - last_update_time > scroll_start_delay){
                scroll_start = true; 
            }else{
                scroll_start = false; 
            }
            last_update_time = now;
            
            var table_top = the_table.offset().top + thead.height() - thepage.scrollTop();
            var table_bottom = the_table.offset().top + the_table.height() - thead.height() - thepage.scrollTop();
            var top = this._get_top_pos();
            
            if( table_top  < top && table_bottom > top){
                this.show(resize)
            }else{
                this.hide()
            }            
        }
        
        methods['show'] = function(resize){
            // align to table, e.g. horiz scrolling
            sticky_table.css({'left':the_table.offset().left-thepage.scrollLeft(), 'top': this._get_top_pos()})
                        
            // make cells same size
            // resize is a bit slow, so only do it at start of scroll or if earlier invisible
            // how do we know scroll started? we track time in update event is time diff > threshold we set scrollStart
            if(resize || scroll_start || !visible){
                this._resize_cells() 
            }   
            sticky_table.show()
            visible = true;
        }
        
        methods['hide'] = function(){
            sticky_table.hide()
            visible = false;
        }
        
        // add all methods to object
        for(method in methods){
            this[method] = methods[method]
        }
        
        // add a way to call methods by name
        this.call_method = function(method){
            if(!method) return;
            return methods[method].apply(this)
        }
        
        //hook events
        thepage.scroll(function(event){
            _this.update()
        })
  
        thepage.resize(function(event){
            _this.update(true)
        })
        
        /// private methods
        this._resize_cells = function(){

            sticky_table.width(the_table.width())
            
            var last_pos = null;
            for(var i=0;i<orig_th_list.length;i++){
                var cw = $(orig_th_list[i]).width();
                $(clone_th_list[i]).width(cw)
                /*var pos = $(orig_th_list[i]).position();
                if(last_pos!==null){
                    ;//$(clone_th_list[i-1]).width(pos.left-last_pos.left)
                }
                last_pos = pos;*/
            }
        }
        
        this._get_top_pos = function(){
            // it is bottom of top menu bar which is fixed at top
            if(settings.top_bar_height!==null) return settings.top_bar_height;
            
            // no top bar given or top bar is not fixed, we align sticky to top of window
            if(top_bar.length==0) return 0;
            if(top_bar.css('position')!='fixed') return 0;
            
            var h = top_bar.offset().top + top_bar.height() - thepage.scrollTop();
            return h;
        }
        
    }
    
})( jQuery );