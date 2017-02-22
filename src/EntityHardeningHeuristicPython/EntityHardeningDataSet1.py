import copy;
import time;

start_time = time.time();
#entities in failed set
FF=['a0','a1','a2','a3','a4','a7','a9','a10','a11','a12','a13','a19','b0','b1',
    'b2','b3','b4','b5','b6','b7','b8','b9','b10','b11','b12','b13',
    'b14','b16'];

#initialize entities after defending in failed set to FF
DFF=copy.deepcopy(FF);

#IDRs containing only failed entities
#[['x0'],['x1','x2'],['x3']] represents the IDR x0 <-x1 x2 +x3
IDR=[[['a0'],['b5'],['b8']],[['a1'],['b5']],[['a2'],['b4'],['b11','b16']],
     [['a3'],['b4'],['b14']],[['a4'],['b9'],['b13']],
     [['b0'],['a0'],['a1']],[['b1'],['a4']],[['b2'],['a0','a7'],['a4']],
     [['b3'],['a0','a9']],[['b4'],['a2','a10'],['a3','a11']],[['b5'],['a1','a12'],['a2','a13']],
     [['b6'],['a1']],[['b7'],['a2'],['a4']],[['b8'],['a1'],['a4']],[['b9'],['a4','a19']],
     [['b10'],['a2'],['a4']],[['b11'],['a0'],['a4']],[['b12'],['a0'],['a3']],
     [['b13'],['a2']],[['b14'],['a4']]];
    
#defenders budget
K_D=7;

#repeat for loop for K_D times each time selecting an entity
for a in range(K_D):
    if len(DFF)>0:
        IDRcopy=[];
        protection=[];#protection set of each entity
        mintermHit=[];#maintains the minterm hit count of each entity
        
        #create a dummy IDR set for each entity currently in failed set
        #add to protection set of each entity the entity itself
        for i in range(len(DFF)):
            IDRcopy.append(copy.deepcopy(IDR));
            protection.append([copy.deepcopy(DFF[i])]);
            mintermHit.append(0);

        #compute protection set and minterm count for defending each entity
        #update IDR accordingly
        for i in range(len(DFF)):
            flag=True;
            pos=0;#current entity of the protection set
            while(flag):
                IDRIntCopy=copy.deepcopy(IDRcopy[i]);
                removePos=0;
                found=False;
                for j in range(len(IDRIntCopy)):
                    #check whether IDR corresponding to current entity in protection set
                    if IDRIntCopy[j][0][0]==protection[i][pos]:
                        removePos=j;
                        found=True;
                    else:
                        #search for protection[pos] in the IDR
                        for l in range(len(IDRIntCopy[j])-1):
                            if len(IDRIntCopy[j][l+1])==1:
                                if IDRIntCopy[j][l+1][0]==protection[i][pos]:
                                    flag1=True;
                                    for m in range(len(protection[i])):
                                        if IDRIntCopy[j][0][0]==protection[i][m]:
                                            flag1=False;

                                    if(flag1):
                                        protection[i].append(IDRIntCopy[j][0][0]);
                                    mintermHit[i]=mintermHit[i]+1;
                            else:
                                for k in range(len(IDRIntCopy[j][l+1])):
                                    if IDRIntCopy[j][l+1][k]==protection[i][pos]:
                                        mintermHit[i]=mintermHit[i]+1;
                                        IDRcopy[i][j][l+1].remove(IDRIntCopy[j][l+1][k]);

                if(found):
                    IDRcopy[i].remove(IDRIntCopy[removePos]);#remove IDR of the entity at pos

                if(len(protection[i])==pos+1):
                    flag=False;
                else:
                    pos=pos+1;


        x=0;#set the entity to be defended to first entity

        #select the entity to be defended 
        for i in range(1,len(DFF)):
            if len(protection[i])>len(protection[x]):
                x=i;
            elif len(protection[i])==len(protection[x]):
                if mintermHit[i]>mintermHit[x]:
                    x=i;
      

        for i in range(len(protection[x])):
            DFF.remove(protection[x][i]);#update the failed set after defending

        #print("Number of entities protected from failure");
        #print("By protecting ",a+1," entities");
        #print(len(FF)-len(DFF),"Entities protected from failure");
        #print(len(DFF),"Entities will fail");
        #print("The entities are");
        #print(DFF);
        #print("With protected entity at current step being - ",protection[x][0]);
        #print("Preventing failure of following entities");
        #print(protection[x]);
        #print('\n');
        
        IDR=copy.deepcopy(IDRcopy[x]);#update the modified IDR
    else:
        print("All entities already protected");

print(time.time() - start_time);
                        
                




        
    
