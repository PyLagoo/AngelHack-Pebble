#include <pebble.h>

static Window *s_window;	
TextLayer *text_layer;
	
// Keys for AppMessage Dictionary
// These should correspond to the values you defined in appinfo.json/Settings
enum {
	STATUS_KEY = 0,	
	MESSAGE_KEY = 1
};

// Write message to buffer & send
static void send_message(void){
	DictionaryIterator *iter;
	
	app_message_outbox_begin(&iter);
	dict_write_cstring(iter, MESSAGE_KEY, "I'm a Pebble!");
	
	dict_write_end(iter);
  app_message_outbox_send();
}



static void accel_tap_handler(AccelAxisType axis, int32_t direction) {
  // A tap event occured
  vibes_short_pulse();
  send_message();
  
}

// Called when a message is received from PebbleKitJS
static void in_received_handler(DictionaryIterator *received, void *context) {
	Tuple *tuple;
	
	tuple = dict_find(received, STATUS_KEY);
	if(tuple) {
		APP_LOG(APP_LOG_LEVEL_DEBUG, "Received Status: %d", (int)tuple->value->uint32); 
	}
	
	tuple = dict_find(received, MESSAGE_KEY);
	if(tuple) {
		APP_LOG(APP_LOG_LEVEL_DEBUG, "Received Message: %s", tuple->value->cstring);
	}
  
  //send_message();
}

// Called when an incoming message from PebbleKitJS is dropped
static void in_dropped_handler(AppMessageResult reason, void *context) {	
}

// Called when PebbleKitJS does not acknowledge receipt of a message
static void out_failed_handler(DictionaryIterator *failed, AppMessageResult reason, void *context) {
}

static void init(void) {
	s_window = window_create();
  text_layer = text_layer_create(GRect(0, 0, 144, 40));
  text_layer_set_text(text_layer, "Hello there!,\nLets Connect... ");
  layer_add_child(window_get_root_layer(s_window),text_layer_get_layer(text_layer));
	window_stack_push(s_window, true);
	
	// Register AppMessage handlers
	app_message_register_inbox_received(in_received_handler); 
	app_message_register_inbox_dropped(in_dropped_handler); 
	app_message_register_outbox_failed(out_failed_handler);

  // Initialize AppMessage inbox and outbox buffers with a suitable size
  const int inbox_size = 128;
  const int outbox_size = 128;
	app_message_open(inbox_size, outbox_size);
  accel_tap_service_subscribe(accel_tap_handler);
}

static void deinit(void) {
  
  accel_tap_service_unsubscribe();
  
	app_message_deregister_callbacks();
	window_destroy(s_window);
}

int main( void ) {
	init();
	app_event_loop();
	deinit();
}