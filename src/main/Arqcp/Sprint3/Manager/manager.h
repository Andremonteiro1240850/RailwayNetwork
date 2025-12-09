#define TRACK_FREE 0
#define TRACK_BUSY 1
#define TRACK_ASSIGNED 2
#define TRACK_INOP 3

typedef struct {
    int id;
    int state;                      // usa valores das defines acima
    int trainId;
    unsigned long lastOperationTimestamp;
} Track;

#define TRAIN_ARRIVING 0
#define TRAIN_STOPPED  1
#define TRAIN_DEPARTING 2

typedef struct {
    int id;
    int state;        // usa valores das defines acima
    int position;
} Train;
