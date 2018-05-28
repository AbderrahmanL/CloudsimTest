package testExamples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.File;
import org.cloudbus.cloudsim.HarddriveStorage;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.util.ExecutionTimeMeasurer;

public class CloudSimExample1 {
	private static final int CLOUDLET_COUNT = 1;
	private static List<Cloudlet> cloudletList;
	private static List<Vm> vmList;
	
	public static void main(String[] args) {
			Log.printLine("Starting simulation...");
			try {
				// initialize the cloudsim library before creating the entities
				int num_user = 1; 
				Calendar calendar = Calendar.getInstance();
				boolean trace_flag = false ;
				
				CloudSim.init(num_user, calendar, trace_flag);
				
				  //Creating 3 Storages
	            HarddriveStorage hd1 = new HarddriveStorage(1024);
	            HarddriveStorage hd2 = new HarddriveStorage(1024);
	            HarddriveStorage hd3 = new HarddriveStorage(1024);

	            //Creating 3 Files
	            //Attention: This is the "org.cloudbus.cloudsim.File" class!!
	            File file1 = new File("file1.dat", 300);
	            File file2 = new File("file2.dat", 300);
	            File file3 = new File("file3.dat", 300);
	            
	            LinkedList<Storage> hdList = new LinkedList<Storage>();
	            hdList.add(hd1);
	            hdList.add(hd2);
	            hdList.add(hd3); 
	            
				Datacenter datacenter0 = createDatacenter("Datacenter_0",hdList);
				datacenter0.addFile(file1);
				datacenter0.addFile(file2);
				datacenter0.addFile(file3);
				// Third step: Create Broker
				DatacenterBroker broker = createBroker();
				int brokerId = broker.getId();

				// Fourth step: Create one virtual machine
				vmList = new ArrayList<Vm>();
	
				// VM description
				int vmid = 0;
				int mips = 1000;
				long size = 100000; // image size (MB)
				int ram = 512; // vm memory (MB)
				long bw = 1000;
				int pesNumber = 1; // number of cpus
				String vmm = "Xen"; // VMM name
	
				// create VM
				Vm vm = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
	
				// add the VM to the vmList
				vmList.add(vm);
	
				// submit vm list to the broker
				broker.submitVmList(vmList);
	
				// Fifth step: Create one Cloudlet
				cloudletList = new ArrayList<Cloudlet>();
			for(int i=0 ; i< CLOUDLET_COUNT ; i++) {
				// Cloudlet properties
				int id = i;
				long length = 40000;
				long fileSize = 300;
				long outputSize = 300;
				UtilizationModel utilizationModel = new UtilizationModelFull();
				List<String> fileList = new ArrayList<String>();
		         fileList.add("file1.dat");
		         fileList.add("file2.dat");
		         fileList.add("file3.dat");
	
				Cloudlet cloudlet = 
	                                new Cloudlet(id, length, pesNumber, fileSize, 
	                                        outputSize, utilizationModel, utilizationModel, 
	                                        utilizationModel);
				cloudlet.setUserId(brokerId);
				cloudlet.setVmId(vmid);
	
				// add the cloudlet to the list
				cloudletList.add(cloudlet);
			}
				// submit cloudlet list to the broker
				broker.submitCloudletList(cloudletList);
				ExecutionTimeMeasurer.start("simulation");
				
				// Sixth step: Starts the simulation
				CloudSim.startSimulation();
				
				CloudSim.stopSimulation();
				
				System.out.println(ExecutionTimeMeasurer.end("simulation"));
				//Final step: Print results when simulation is over
				List<Cloudlet> newList = broker.getCloudletReceivedList();
				printCloudletList(newList);
				printNotSuccess(newList);
				 System.out.println("* * *");
		            System.out.println("Used disk space on hd1=" + hd1.getCurrentSize());
		            System.out.println("Used disk space on hd2=" + hd2.getCurrentSize());
		            System.out.println("Used disk space on hd3=" + hd3.getCurrentSize());
		            System.out.println("* * *");
		            System.out.println(newList.get(0).getCloudletOutputSize()); 
				Log.printLine("CloudSimExample1 finished!");
				
			}
			catch(Exception e) {
				e.printStackTrace();
			}
	}

	private static void printNotSuccess(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);

			if (cloudlet.getCloudletStatus() != Cloudlet.SUCCESS) {
				Log.print("NotSuccess");	
			}
		}
		
	}

	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time" + indent
				+ "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
			else
			Log.print("FAILURE");
		}
	}

	private static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	private static Datacenter createDatacenter(String name, LinkedList<Storage> hdList) {

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		// our machine
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();

		int mips = 1000;

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

		// 4. Create Host with its id and list of PEs and add them to the list
		// of machines
		int hostId = 0;
		int ram = 2048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 10000;

		hostList.add(
			new Host(
				hostId,
				new RamProvisionerSimple(ram),
				new BwProvisionerSimple(bw),
				storage,
				peList,
				new VmSchedulerSpaceShared(peList)
			)
		); // This is our machine

		// 5. Create a DatacenterCharacteristics object that stores the
		// properties of a data center: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/Pe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = hdList;

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}
	
	
}
