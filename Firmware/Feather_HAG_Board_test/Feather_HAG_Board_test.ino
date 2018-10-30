#include <bluefruit.h>

BLEService        hags = BLEService(0x53e74e5ad19247a08f0635ec90c73a3a);
BLECharacteristic hagd = BLECharacteristic(0x655013bc5e864d908b609eff874e888d); //depth
BLECharacteristic haga = BLECharacteristic(0x8a013e8fddcc4498a8f7579e923fda72); //angle
BLECharacteristic hagw = BLECharacteristic(0x9a9ee845ae63476ca3dd183c9de1cb2e); //wieght
BLECharacteristic hagm = BLECharacteristic(0x46aa885c4bd847dc95830c3f6639f4d4); //move

BLEDis  bledis;
BLEUart bleuart;
BLEBas  blebas;

uint8_t angel = 0;
uint8_t depth = 0;
uint8_t wieght = 0;
uint8_t battery = 0;

void connect_callback(uint16_t conn_handle);
void disconnect_callback(uint16_t conn_handle, uint8_t reason);

void setup()
{
  Serial.begin(115200);

  Serial.println("H.A.G. Board");
  Serial.println("------------\n");

  // Initialise the Bluefruit module
  Serial.println("Initialise the Bluefruit nRF52 module");
  Bluefruit.begin();

  // Set the advertised device name
  Serial.println("Setting Device Name to 'H.A.G. Board'");
  Bluefruit.setName("H.A.G. Board");

  // Set the connect/disconnect callback handlers
  Bluefruit.setConnectCallback(connect_callback);
  Bluefruit.setDisconnectCallback(disconnect_callback);

  // Setup the H.A.G. Board service using
  // BLEService and BLECharacteristic classes
  Serial.println("Configuring the H.A.G. Board Service");
  setupHAG();

  // Set up Advertising Packet
  Serial.println("Setting up the advertising payload(s)");
  startAdv();

  Serial.println("\nAdvertising");
}

void startAdv(void)
{
  Bluefruit.Advertising.addFlags(BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE);
  Bluefruit.Advertising.addTxPower();

  // Include H.A.G. board 128-bit uuid
  Bluefruit.Advertising.addService(hags);

  // There is no room for Name in Advertising packet
  // Use Scan response for Name
  Bluefruit.ScanResponse.addName();

  /* Start Advertising
   * - Enable auto advertising if disconnected
   * - Interval:  fast mode = 20 ms, slow mode = 152.5 ms
   * - Timeout for fast mode is 30 seconds
   * - Start(timeout) with timeout = 0 will advertise forever (until connected)
   * 
   * For recommended advertising interval
   * https://developer.apple.com/library/content/qa/qa1931/_index.html   
   */
  Bluefruit.Advertising.restartOnDisconnect(true);
  Bluefruit.Advertising.setInterval(32, 244);    // in unit of 0.625 ms
  Bluefruit.Advertising.setFastTimeout(30);      // number of seconds in fast mode
  Bluefruit.Advertising.start(0);                // 0 = Don't stop advertising after n seconds  
}

void setupHAG() {
  
  // Configure and Start HAG Service
  hags.begin();

  // Start depth Service
  hagd.setProperties(CHR_PROPS_NOTIFY);
  hagd.setPermission(SECMODE_OPEN, SECMODE_NO_ACCESS);
  hagd.setFixedLen(1);
  hagd.begin();
  uint8_t hagdata [1] = {0x00};
  hagd.notify(hagdata, 1);

  // Start angle Service
  haga.setProperties(CHR_PROPS_NOTIFY);
  haga.setPermission(SECMODE_OPEN, SECMODE_NO_ACCESS);
  haga.setFixedLen(1);
  haga.begin();
  hagdata [1] = {0x00};
  hagd.notify(hagdata, 1);

  // Start wieght Service
  hagw.setProperties(CHR_PROPS_NOTIFY);
  hagw.setPermission(SECMODE_OPEN, SECMODE_NO_ACCESS);
  hagw.setFixedLen(1);
  hagw.begin();
  hagdata [1] = {0x00};
  hagw.notify(hagdata, 1);
  
  // Start move Service
  hagm.setProperties(CHR_PROPS_READ);
  hagm.setPermission(SECMODE_OPEN, SECMODE_NO_ACCESS);
  hagm.setFixedLen(1);
  hagm.begin();
}

void loop()
{
  digitalToggle(LED_RED);

  if ( Bluefruit.connected() ) {
    uint8_t hagdata [1] = {depth++};
    if ( hagd.notify(hagdata, 1) ){
      Serial.print("Depth updated to: "); Serial.println(depth); 
    }else{
      Serial.println("ERROR: Notify not set in the CCCD or not connected!");
    }
  }

  
  delay(1000);


}

void connect_callback(uint16_t conn_handle)
{
  char central_name[32] = { 0 };
  Bluefruit.Gap.getPeerName(conn_handle, central_name, sizeof(central_name));

  Serial.print("Connected to ");
  Serial.println(central_name);
}

void disconnect_callback(uint16_t conn_handle, uint8_t reason)
{
  (void) conn_handle;
  (void) reason;

  Serial.println();
  Serial.println("Disconnected");
  Serial.println("Bluefruit will start advertising again");
}

/**
 * RTOS Idle callback is automatically invoked by FreeRTOS
 * when there are no active threads. E.g when loop() calls delay() and
 * there is no bluetooth or hw event. This is the ideal place to handle
 * background data.
 * 
 * NOTE: FreeRTOS is configured as tickless idle mode. After this callback
 * is executed, if there is time, freeRTOS kernel will go into low power mode.
 * Therefore waitForEvent() should not be called in this callback.
 * http://www.freertos.org/low-power-tickless-rtos.html
 * 
 * WARNING: This function MUST NOT call any blocking FreeRTOS API 
 * such as delay(), xSemaphoreTake() etc ... for more information
 * http://www.freertos.org/a00016.html
 */
void rtos_idle_callback(void)
{
  // Don't call any other FreeRTOS blocking API()
  // Perform background task(s) here
}
