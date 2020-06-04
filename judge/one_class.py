import pandas as pd 
import numpy as np 
import os

class One_auth:

    def extract_fft(self,df):
        # Normalization
        normalized_df=(df-df.mean())/df.std()
        z_values = normalized_df.z.to_numpy()

        # alignment
        base_signal = np.zeros(100)
        base_signal[0:50]=1
        base_signal[50:100]=-1
        co = np.correlate(z_values,base_signal,mode='same')
        max_index = np.argmax(co)
        segment = normalized_df.shift(periods=256-max_index,fill_value=0).z.to_numpy()

        # FFT
        fft_z =  np.fft.fft(segment,n=512)
        abs_fft_z = np.abs(fft_z)

        return abs_fft_z[0:256]

    def fit(self):
        y_train_label=[]
        features=[]
        for root, dirs, files in os.walk('data'):
            for filename in files:
                fullname = os.path.join(root,filename)
                if filename.endswith('.csv'):
                    df = pd.read_csv(fullname,names=['x','y','z'])
                    normalized_df=(df-df.mean())/df.std()
                    feature = self.extract_fft(df)
                    features.append(feature)
                    y_train_label.append(filename[0:8])
                    # if(filename.startswith(hand)):
                    #     y_train_label.append(1)
                    # else:
                    #     y_train_label.append(-1)
        raw_X_train = np.array(features)

        # y_test_label=[]
        # features=[]
        # for root, dirs, files in os.walk('testdata'):
        #     for filename in files:
        #         fullname = os.path.join(root,filename)
        #         if filename.endswith('.csv'):
        #             df = pd.read_csv(fullname,names=['x','y','z'])
        #             normalized_df=(df-df.mean())/df.std()
        #             feature = self.extract_fft(df)
        #             features.append(feature)
        #             y_test_label.append(filename[0:8])
        #             # if(filename.startswith(hand)):
        #             #     y_test_label.append(1)
        #             # else:
        #             #     y_test_label.append(-1)
        # raw_X_test = np.array(features)

        # np.savetxt(fname='x.tsv',X=raw_X_train,delimiter='\t')
        # np.savetxt(fname='x_meta.tsv',X=y_train_label,delimiter='\t')

        # from sklearn.preprocessing import MaxAbsScaler
        # scaler = MaxAbsScaler()
        # X_train = scaler.fit_transform(raw_X_train)
        X_train = raw_X_train

        from sklearn.preprocessing import LabelEncoder
        self.le = LabelEncoder()
        y_train_label=self.le.fit_transform(y_train_label)

        #UMAP
        import umap
        self.reducer = umap.UMAP(n_neighbors=6,n_components=3,min_dist=1)
        X = self.reducer.fit_transform(X_train,y=y_train_label)
        # plt.scatter(X[:,0],X[:,1],c=y_train_label,s=5)
        # plt.show()


        # from metric_learn import LMNN
        # lmnn = LMNN(k=3)
        # lmnn.fit(X_train,y_train_label)
        # X=lmnn.transform(X_train)

        from sklearn.svm import SVC
        self.clf = SVC(C=1)
        # clf=SVC(kernel='linear')
        self.clf.fit(X,y_train_label)

        # from sklearn.metrics import accuracy_score,classification_report,confusion_matrix

        # X_test=reducer.transform(raw_X_test)
        # y_test_label=self.le.transform(y_test_label)
        # plt.scatter(X_test[:,0],X_test[:,1],c=y_test_label,s=5)
        # plt.show()

        # X_test=lmnn.transform(raw_X_test)

        # y_pred = clf.predict(X_test)

        # print(accuracy_score(y_test_label,y_pred))
        # print(classification_report(y_test_label,y_pred))
        # print(confusion_matrix(y_test_label,y_pred))

    def predict(self,new_sample,requester):
        raw_y=[]
        raw_y.append(requester)
        test_label=self.le.transform(raw_y)
        new_raw_X = self.extract_fft(new_sample)
        X = self.reducer.transform(np.reshape(new_raw_X,(1,-1)))
        result = self.clf.predict(X)[0]
        if(result==test_label):
            return True
        else:
            return False