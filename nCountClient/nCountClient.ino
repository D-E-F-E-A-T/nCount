#include <ESP8266WiFi.h>
#include <SoftwareSerial.h>
#include <EEPROM.h>
#include <ESP8266WiFi.h>        // Include the Wi-Fi library

#define HOST "192.168.1.126"
#define PORT 27374
#define IDNUM 1

//////////////////////////////////////
// USER CONFIGURABLE SETTINGS BELOW //
//////////////////////////////////////

const char* ssid     = "ViruStop";         // The SSID (name) of the Wi-Fi network you want to connect to
const char* password = "tczaflw@747";     // The password of the Wi-Fi network

uint16_t timesActivated = 0;

String macID;

void setup()
{
  initHardware();
  initEEPROM();
  setupWiFi();
}

void loop()
{
  readSensors();
  runServer();
}

bool flag = false;

void readSensors()
{
  if (analogRead(A0) < 900)
  {
    flag = true;
  }

  if (analogRead(A0) >= 900 && flag) 
  {
    timesActivated++;
    Serial.println(timesActivated);
    flag = false;
  }
}

WiFiClient client;
bool authStatus = false;

void runServer()
{
  if(client && client.connected())
  {
    // authenticate and listen for commands from server.
    if (!authStatus)
    {
      // send the authentication command "AUTH [ID] [MacAddr]"
      // if we have successfully authenticated (response back is "auth_successful") then set authStatus to true. Next time runServer() is called.
      Serial.println("Authenticating...");
      String printStr;
      printStr += "AUTH ";
      printStr += IDNUM;
      printStr += " ";
      printStr += macID;
      printStr += "\n";
      client.print(printStr);
      delay(1);
      Serial.println("Done.");
      authStatus = true;
    }
    else
    {
       String req = client.readStringUntil('\r');
       Serial.println(req);
       String toSend = "";
       if (req.indexOf("query_success") != -1)
       {
        Serial.println("Resetting Times Activated");
        timesActivated = 0;
       }
       else if (req.indexOf("request_query") != -1)
       {
        toSend += "add_num_people ";
        toSend += timesActivated;
        toSend += "\n";
        client.print(toSend);
        delay(1);
       }
       
       // responses to requests
       // reset_number should reset the sensor to zero
       // request_query should cause the device to send "add_num_people [number of people counted]" and then reset the sensor to zero
       // 
    } 
  }
  else
  {
    authStatus = false;
    Serial.println("No connection yet, beginning connection...");
    client.connect(HOST, PORT);
  }

}


void setupWiFi() 
{
  WiFi.begin(ssid, password);             // Connect to the network
  uint8_t mac[WL_MAC_ADDR_LENGTH];
  WiFi.softAPmacAddress(mac);
  macID = String(mac[WL_MAC_ADDR_LENGTH - 2], HEX) +
                 String(mac[WL_MAC_ADDR_LENGTH - 1], HEX);
  macID.toUpperCase();
  Serial.print("MAC ID: ");
  Serial.println(macID);
  Serial.print("Connecting to WiFi SSID: \"");
  Serial.print(ssid); Serial.println("\".");

  int i = 0;
  while (WiFi.status() != WL_CONNECTED)
  {
    delay(1000);
    i++;
    Serial.print(i);
    Serial.println();
  }
  
  Serial.println('\n');
  Serial.println("Connection established!");  
  Serial.print("ESP8266 IP address:\t");
  Serial.println(WiFi.localIP());
}

void writeEEPROM(int pos, int val)
{
  int actualpos = pos * 2;
  EEPROM.write(actualpos, highByte((int)val));
  EEPROM.write(actualpos + 1, lowByte((int)val));
  EEPROM.commit();
}

int readEEPROM(int pos)
{
  int actualpos = pos * 2;
  int ad;
  byte h = EEPROM.read(actualpos);
  byte l = EEPROM.read(actualpos + 1);
  ad = (h << 8) + l;
  return ad;
}

void initEEPROM()
{
  Serial.println("EEPROM initialized.");
  EEPROM.begin(0x200);
}

void initHardware()
{
  Serial.begin(115200);
  Serial.println("Serial monitor initialized with 115200 baud.");
}

