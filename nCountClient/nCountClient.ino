#include <ESP8266WiFi.h>
#include <SoftwareSerial.h>
#include <EEPROM.h>
#include <ESP8266WiFi.h>        // Include the Wi-Fi library

#define HOST "192.168.1.125"
#define PORT 80

//////////////////////////////////////
// USER CONFIGURABLE SETTINGS BELOW //
//////////////////////////////////////

const char* ssid     = "WiFi";         // The SSID (name) of the Wi-Fi network you want to connect to
const char* password = "password";     // The password of the Wi-Fi network

uint32_t timesActivated = 0;

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
  if(!client.connected())
  {
    Serial.println("No connection yet, beginning connection...");
    if (!client.connect(HOST, PORT))
    {
      Serial.println("Connection failed");
      authStatus = false;
    }
    else
    {
      Serial.println("Connection success");
    }
  }
  if(client.connected())
  {
    // authenticate and listen for commands from server.
    if (!authStatus)
    {
      // send the authentication command "AUTH [ID] [MacAddr]"
      // if we have successfully authenticated (response back is "auth_successful") then set authStatus to true. Next time runServer() is called, since we have authenticated we just listen for commands from server.
    }
    else
    {
      
    }
    
  }

}


void setupWiFi() 
{
  WiFi.begin(ssid, password);             // Connect to the network
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

