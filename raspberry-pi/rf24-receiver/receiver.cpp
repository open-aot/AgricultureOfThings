#include <cstdlib>
#include <ctime>
#include <iostream>
#include <sstream>
#include <string>
#include <unistd.h>
#include <RF24/RF24.h>
#include <fstream>
using namespace std;


RF24 radio(RPI_V2_GPIO_P1_15,RPI_V2_GPIO_P1_24 ,BCM2835_SPI_SPEED_8MHZ);

int last_timestamp = 0;

void save_data(char* result);

void write_to_file (string path, char* data);

float remap_value(float x, float in_min, float in_max, float out_min, float out_max);

string timeStampToHReadble(const time_t rawtime);

float convert_moisture(int t);
int
main(int argc, char** argv)
{
	int second = 1000000;
	int minute = 60 * second;

	char text[64]="";
	radio.begin();
	const uint8_t pipes[6] = "1Node";
	radio.openReadingPipe(1,pipes);
	radio.startListening();
	while(1){
		if(radio.available()){
			radio.read(&text,32 );
			printf("%s\n",text);
			save_data(text);
			radio.stopListening();
			cout << "Sleeping\n";
			usleep(4 * minute);
			cout << "Wake up\n";
			radio.startListening();
		}else{
			printf("Waiting data...\n");
			usleep(second);
		}
	}
	return 0;
}

void
save_data(char* result){
	int msgNum, m1, h, t;
	float mp1;
	char json[128] = "", json2[128]="", raw[10000]="";
	sscanf(result, "%d, %d, %d, %d, %d", &msgNum, &m1, &h, &t);

	time_t timestamp = time(nullptr);

	int curr_timestamp = (int) timestamp;

	sprintf(raw, "{\"MessageNum\" : %d, \"Timestamp\" : %d, \"Moisture1\" : %d, \"Humidity\" : %d, \"Temperature\" : %.d},", msgNum, (int) timestamp, m1, h, t);
	write_to_file ("../data/raw_data.txt", raw);

	mp1 = convert_moisture(m1);

	string dateStr = timeStampToHReadble(curr_timestamp);
	sprintf(json, "{\"Timestamp\" : \"%s\", \"Moisture\" : %.2f, \"Humidity\" : %d, \"Temperature\" : %d},",dateStr.c_str(), mp1, h, t);
	write_to_file("../data/results.txt", json);

}

void write_to_file (string path, char* data){
		ofstream ofs;
                ofs.open (path, ofstream::out | ofstream::app);
                ofs << "\t" << data << endl;
                ofs.close();
}

float convert_moisture(int t){
	return remap_value((float) t, 590.0, 380.0, 0.0, 100.0);
}

float remap_value(float x, float in_min, float in_max, float out_min, float out_max) {
  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}

string timeStampToHReadble(const time_t rawtime)
{
    struct tm * dt;
    char buffer [30];
    dt = localtime(&rawtime);
    strftime(buffer, sizeof(buffer), "%a %d %b %H:%M:%S %Z %Y", dt);
    return string(buffer);
}
