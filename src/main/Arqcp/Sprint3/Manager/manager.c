#include "manager.h"
#include "Board.h"
#define MAX_TRAINS 100

int main (){
    Track tracks[10]; // tenho depois de alterar pois isso é dito no ficheiro da us 11
    Train *trains = malloc(sizeof(Train) * MAX_TRAINS);
}

int assignArrivingTrain (Train *t, Track *tr) {
    if(t->state != 0 ){
        return 0;
    }
    if(tr-> state == 0){
        tr-> state = 2;
        t -> state = 2;
        freeTrack();
        return 1;
        //falta enviar o comando para light sins
    } else {
        showEmergencyStop();
        return 0;
    }
}
int setTrackINOP(Track *t){
    if(t->state == 3){
        trackINOP();
        return 1;
    } else {
        trackINOP();
        t->state = 3;
        return 1;
    }
}