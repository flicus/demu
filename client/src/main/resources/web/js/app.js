Vue.component('Sessions', {
    template: '<main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">\n' +
        '        <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3">\n' +
        '            <h1 class="h2">New session</h1>\n' +
        '            <div class="btn-toolbar mb-2 mb-md-0">\n' +
        '                <!--                    <div class="btn-group mr-2">-->\n' +
        '                <!--                        <button type="button" class="btn btn-sm btn-outline-secondary">Share</button>-->\n' +
        '                <!--                        <button type="button" class="btn btn-sm btn-outline-secondary">Export</button>-->\n' +
        '                <!--                    </div>-->\n' +
        '                <button type="button" class="btn btn-sm btn-primary" v-on:click="startDiameter">\n' +
        '                    <i class="fa fa-bolt"></i>\n' +
        '                    Start\n' +
        '                </button>\n' +
        '            </div>\n' +
        '        </div>\n' +
        '        <!--            <h2>Section title</h2>-->\n' +
        '        <div class="table-responsive">\n' +
        '            <table class="table table-striped table-sm">\n' +
        '                <thead>\n' +
        '                <tr>\n' +
        '                    <th>#</th>\n' +
        '                    <th>Session</th>\n' +
        '                    <th>Message type</th>\n' +
        '                    <th>Result code</th>\n' +
        '                    <th>Status</th>\n' +
        '                </tr>\n' +
        '                </thead>\n' +
        '                <tbody>\n' +
        '                <tr v-for="item in requests">\n' +
        '                    <td><i v-if="item[\'is-request\']" class="fa fa-lg fa-arrow-right"></i></td>\n' +
        '                    <td>{{ item[\'session-id\'] }}</td>\n' +
        '                    <td>{{ uiRepresent(item[\'is-request\'], item.avps[\'CC-Request-Type\'].value) }}</td>\n' +
        '                    <td><div v-if="!item[\'is-request\']">{{ item.avps[\'Result-Code\'].value }}</div></td>\n' +
        '                    <td>\n' +
        '                        <i v-if="!item[\'is-request\'] && item.avps[\'Result-Code\'].value == 2001" class="fa fa-lg fa-check-circle-o text-success" ></i>\n' +
        '                        <i v-if="!item[\'is-request\'] && item.avps[\'Result-Code\'].value != 2001" class="fa fa-lg fa-exclamation-circle text-danger"></i>\n' +
        '                    </td>\n' +
        '                </tr>\n' +
        '                </tbody>\n' +
        '            </table>\n' +
        '        </div>\n' +
        '    </main>',
    props: ['config', 'requests'],
    methods: {
        startDiameter: function () {
            console.log('starting session');
            // eventBus.send('diameter.api', {action:'start', target:'session'});
            eventBus.send('diameter.api', {action:'init', target:'session'});
        },
        uiRepresent: function (isRequest, requestType) {
            var end;
            switch (requestType) {
                case 1:
                    end = "I";
                    break;
                case 2:
                    end = "U";
                    break;
                case 3:
                    end = "T";
                    break;
            }
            return "CC" + (isRequest ? "R-" : "A-") + end;
        }
    }
});

Vue.component('Settings', {
    template: '<main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">\n' +
        '        <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3">\n' +
        '            <form>\n' +
        '                <div class="form-group">\n' +
        '                    <label for="remotePeer">Remote peer</label>\n' +
        '                    <input class="form-control" id="remotePeer" aria-describedby="remotePeerHelp" v-model="config.REMOTE_PEER">\n' +
        '                    <small id="remotePeerHelp" class="form-text text-muted">Remote server URI, eg aaa://192.168.0.5:3838</small>\n' +
        '                </div>\n' +
        '                <button type="button" class="btn btn-primary" v-on:click="$emit(\'save-cfg\')">Save</button>\n' +
        '            </form>\n' +
        '        </div>\n' +
        '    </main>',
    props: ['config', 'requests'],
    methods: {

    }
});

var eventBus;
var app = new Vue({
    el: '#app',
    data: {
        currentView: 'Sessions',
        requests: [],
        config: {}
    },
    created: function () {
        console.log('vue app started');
        var that = this;
        eventBus = new EventBus('http://localhost:9090/eventbus');
        eventBus.onopen = function () {
            console.log('eventbus opened');
            eventBus.registerHandler('diameter.events', function (error, message) {
                console.log('received a message: ' + JSON.stringify(message));
                // console.log(that);
                that.requests.push(message.body);
            });
            console.log('getting configuration');
            eventBus.send('config', {action:'get'}, {}, function (error, resp) {
                if (error == null) {
                    console.log("config received:");
                    console.log(resp.body);
                    that.config = resp.body;
                } else {
                    console.error(error)
                }
            });
        }
    },
    computed: {
        currentComponent: function () {
            return this.currentView;
        }
    },
    methods: {
        saveConfig: function () {
            console.log('saving config');
            eventBus.send('config', {action:'set', body: this.config}, {}, function (error, resp) {
                if (error == null) {
                    console.log("config saved:");
                    console.log(resp.body);
                } else {
                    console.error(error)
                }
            });
        }
    }
});