%Reads in data and calculates N^th term of Mexp to validate spark/mkl

function res = kmv_test(Stub, Npow)


MatFile = sprintf('%s_Matrix.txt',Stub);
InVecFile = sprintf('%s_Vector.txt',Stub);
OutVecFile = sprintf('%s_Outvec.txt',Stub);


Ae = getData(MatFile,3);
ve = getData(InVecFile,2);
ove= getData(OutVecFile,2);

[A,N] = toMatrix(Ae);

v = toVector(ve,N);
Evs = full(toVector(ove,N));

Eap = expmApprox(A,Npow);
E = expm(A);

Eapv = Eap*v;
Ev = E*v;

res.app = norm(Eapv-Evs)/norm(Ev);
res.true = norm(Evs-Ev)/norm(Ev);

fprintf('Relative error between Matlab/Spark approximations = %e\n', res.app);
fprintf('Relative error between Spark/Real answer = %e\n', res.true);
fprintf('Norm of true answer = %e\n', norm(Ev));

function Eap = expmApprox(A,Npow)
[N M] = size(A);

Eap = eye(N) +  A;
En = A;
for n=2:Npow
    En = En*A/n;
    Eap = Eap + En;
end

function En = PowTerm(A,Npow)

En = A;
for n=2:Npow
    En = En*A/n;
end
function [A,N] = toMatrix(Ae)

Nr = max(Ae(1,:));
Nc = max(Ae(2,:));
N = 1+max(Nr,Nc);

ii = 1+Ae(1,:);
jj = 1+Ae(2,:);
s = Ae(3,:);

A = sparse(ii,jj,s,N,N);

function v = toVector(ve,N)
ii = 1+ve(1,:);
s = ve(2,:);
nz = length(s);
jj = 1*ones(nz,1);
v = sparse(ii,jj,s,N,1);

function Ze = getData(File,nind)
fid = fopen(File,'r');

if (nind == 2)
    fspc = '%d\t%f';
    sizeZ = [2 Inf];
elseif (nind == 3)
    fspc = '%d\t%d\t%f';
    sizeZ = [3 Inf];
end

Ze = fscanf(fid,fspc,sizeZ);
fclose(fid);

