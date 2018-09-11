import java.util.ArrayList;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Color;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.LinkedList;
import java.lang.Math;
import java.util.*;
class Agent {
	MyPlanner plan = new MyPlanner();
	// The initial state for the robot.
	int goalX = 100;
	int goalY= 100;
	boolean performUCS = false;
	LinkedList<GameState> linePath;
	Agent()
	{
		linePath = new LinkedList<>();
	}
	void drawPlan(Graphics g, Model m) {
		g.setColor(Color.red);
		for(int i = 0; i < linePath.size() - 1; i++)
		{
			g.drawLine((int)linePath.get(i).state[0], (int)linePath.get(i).state[1], (int)linePath.get(i+1).state[0], (int)linePath.get(i+1).state[1]);
		}
		g.setColor(Color.yellow);
		if(plan.getFrontier() != null)
		{
			while(!plan.getFrontier().isEmpty())
			{
				GameState current = plan.getFrontier().poll();
				g.fillOval((int)current.state[0], (int)current.state[1], 10, 10);
			}
		}
	}
	void update(Model m)
	{
		GameState path = new GameState(0.0f, null, m);
		GameState goal = new GameState(0.0f, null, m);
		path.state[0] = Math.round(m.getX()/10) * 10;
		path.state[1] = Math.round(m.getY()/10) * 10;
		Controller c = m.getController();
		while(true)
		{
			MouseEvent e = c.nextMouseEvent();
			if(e == null)
			{
				break;
			}
			if(e.getButton() == 1)
			{
				performUCS = true;
			}
			else if(e.getButton() == 3)
			{
				performUCS = false; // A* search.
			}
			goalX = Math.round(e.getX() / 10) * 10;
			goalY = Math.round(e.getY() / 10) * 10;
			m.setDestination(e.getX(), e.getY());
		}
		goal.state[0] = goalX;
		goal.state[1] = goalY;
		if(path.state[0] != goal.state[0] && path.state[1] != goal.state[1])
		{
			if(performUCS)
			{
				System.out.println("Uniform Cost");
				GameState ucs = plan.uniformCostSearch(path, goal);
				linePath = GameState.getSuccessors(ucs);
				int size = linePath.size();
				//System.out.println(size);
				if(linePath.size() > 1)
					path = linePath.get(1);
				else
					path = linePath.get(0);
			}
			else
			{
				System.out.println("A* Search");
				GameState AStar = plan.AStarSearch(path, goal, m);
				linePath = GameState.getSuccessors(AStar);
				if(linePath.size() > 1)
					path = linePath.get(1);
				else
					path = linePath.get(0);
			}
			m.setDestination(Math.round(path.state[0]/10) * 10, Math.round(path.state[1]/10) * 10);
		}
		//System.out.println("Result has been found");
	}

	public static void main(String[] args) throws Exception
	{
		Controller.playGame();
	}
}
class GameState
{
	public float cost;
	GameState parent;
	float[] state;
	Model model;
	GameState(float c, GameState p, Model m)
	{
		this.cost = c;
		this.parent = p;
		this.state = new float[2];
		if(p != null)
		{
			this.state[0] = p.state[0];
			this.state[1] = p.state[1];
		}
		else
		{
			this.state[0] = 100.0f;
			this.state[1] = 100.0f;
		}
		this.model = m;
	}
	GameState(Model m)
	{
		this.cost = 0.0f;
		this.parent = null;
		this.state = new float[2];
		this.state[0] = m.getX();
		this.state[1] = m.getY();
		this.model = m;
	}
	public static LinkedList<GameState> getSuccessors(GameState gs)
	{
		GameState temp = gs;
		LinkedList<GameState> results = new LinkedList<GameState>();
		results.add(temp);
		while(temp.parent != null)
		{
			results.add(temp.parent);
			temp = temp.parent;
		}
		LinkedList<GameState> correctResult = new LinkedList<GameState>();
		for(int i = results.size() - 1; i >= 0; i--)
		{
			correctResult.add(results.get(i));
		}
		return correctResult;
	}
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("X: " + this.state[0]);
		sb.append(" Y: " + this.state[1]);
		sb.append("\n");
		return sb.toString();
	}
}
class MyPlanner
{
	TreeSet<GameState> visited;
	public PriorityQueue<GameState> frontier;
	CostComparator costComp = new CostComparator();
	StateComparator stateComp = new StateComparator();
	GameState uniformCostSearch(GameState start, GameState goal)
	{
		frontier = new PriorityQueue<GameState>(costComp);
		visited = new TreeSet<GameState>(stateComp);
		start.cost = 0.0f;
		start.parent = null;
		visited.add(start);
		frontier.add(start);
		while(frontier.size() > 0)
		{
			GameState s = frontier.poll();
			if(s.state[0] == goal.state[0] && s.state[1] == goal.state[1])
			{
				// Also flush queue before returning the goal state.
				return s;
			}
			for(int i = 0; i < 8; i++)
			{
				GameState child = transition(s, i);
				// float acost = actionCost(s, i);
				if(child != null)
				{
					if(visited.contains(child))
					{
						GameState oldChild = visited.floor(child);
						if(s.cost + child.cost < oldChild.cost)
						{
							oldChild.cost = s.cost + child.cost;
							oldChild.parent = s;
						}
					}
					else
					{
						//child.cost = s.cost + child.cost;
						child.parent = s;
						frontier.add(child);
						visited.add(child);
					}
				}
			}
		}
		throw new RuntimeException("There is no such path to the goal");
	}
	PriorityQueue<GameState> getFrontier()
	{
		return this.frontier;
	}
	GameState AStarSearch(GameState start, GameState goal, Model m)
	{
		frontier = new PriorityQueue<GameState>(costComp);
		visited = new TreeSet<GameState>(stateComp);
		start.cost = 0.0f;
		start.parent = null;
		visited.add(start);
		frontier.add(start);
		while(frontier.size() > 0)
		{
			GameState s = frontier.poll();
			if(s.state[0] == goal.state[0] && s.state[1] == goal.state[1])
			{
				// Also flush queue before returning the goal state.
				return s;
			}
			float heurstic = (float)(1 / m.getTravelSpeed(100, 100));
			heurstic *= m.getDistanceToDestination(0);
			for(int i = 0; i < 8; i++)
			{
				GameState child = transition(s, i);
				if(child != null)
				{
					child.cost += heurstic;
					if(visited.contains(child))
					{
						GameState oldChild = visited.floor(child);
						if(s.cost + child.cost < oldChild.cost)
						{
							oldChild.cost = s.cost + child.cost;
							oldChild.parent = s;
						}
					}
					else
					{
						//child.cost = s.cost + child.cost;
						child.parent = s;
						frontier.add(child);
						visited.add(child);
					}
				}
			}
		}
		throw new RuntimeException("There is no such path to the goal");
	}
	GameState transition(GameState prev, int a)
	{
		GameState trans = new GameState(prev.cost, prev, prev.model);
		if(a == 0) // x+10, y-10
		{
			trans.state[0] += 10;
			trans.state[1] -= 10;
			if(trans.state[0] >= 0.0f && trans.state[0] <= Model.XMAX && trans.state[1] >= 0.0f && trans.state[1] <= Model.YMAX)
			{
				trans.cost += Math.sqrt(2) / (trans.model.getTravelSpeed(trans.state[0], trans.state[1]));
				return trans;
			}
			else
				return null;
		}
		else if(a == 1) // x+10, y
		{
			
			trans.state[0] += 10;
			if(trans.state[0] >= 0.0f && trans.state[0] <= Model.XMAX && trans.state[1] >= 0.0f && trans.state[1] <= Model.YMAX)
			{
				trans.cost += 1 / (trans.model.getTravelSpeed(trans.state[0], trans.state[1]));
				return trans;
			}
			else 
				return null;
		}
		else if(a == 2) // x+10, y+10
		{
			
			trans.state[0] += 10;
			trans.state[1] += 10;
			if(trans.state[0] >= 0.0f && trans.state[0] <= Model.XMAX && trans.state[1] >= 0.0f && trans.state[1] <= Model.YMAX)
			{
				trans.cost += Math.sqrt(2) / (trans.model.getTravelSpeed(trans.state[0], trans.state[1]));
				return trans;
			}
			else
				return null;
		}
		else if(a == 3) // x, y+10
		{
			
			trans.state[1] += 10;
			if(trans.state[0] >= 0.0f && trans.state[0] <= Model.XMAX && trans.state[1] >= 0.0f && trans.state[1] <= Model.YMAX)
			{
				trans.cost += 1 / (trans.model.getTravelSpeed(trans.state[0], trans.state[1]));
				return trans;
			}
			else
				return null;
		}
		else if(a == 4) // x, y-10
		{
			
			trans.state[1] -= 10;
			if(trans.state[0] >= 0.0f && trans.state[0] <= Model.XMAX && trans.state[1] >= 0.0f && trans.state[1] <= Model.YMAX)
			{
				trans.cost += 1 / (trans.model.getTravelSpeed(trans.state[0], trans.state[1]));
				return trans;
			}
			else
				return null;
		}
		else if(a == 5)// x-10, y-10
		{
			
			trans.state[0] -= 10;
			trans.state[1] -= 10;
			if(trans.state[0] >= 0.0f && trans.state[0] <= Model.XMAX && trans.state[1] >= 0.0f && trans.state[1] <= Model.YMAX)
			{
				trans.cost += Math.sqrt(2) / (trans.model.getTravelSpeed(trans.state[0], trans.state[1]));
				return trans;
			}
			else
				return null;
		}
		else if(a == 6) // x-10, y
		{
			
			trans.state[0] -= 10;
			if(trans.state[0] >= 0.0f && trans.state[0] <= Model.XMAX && trans.state[1] >= 0.0f && trans.state[1] <= Model.YMAX)
			{
				trans.cost += 1 / (trans.model.getTravelSpeed(trans.state[0], trans.state[1]));
				return trans;
			}
			else
				return null;
		}
		else if(a == 7) // x-10, y+10
		{
			
			trans.state[0] -= 10;
			trans.state[1] += 10;
			if(trans.state[0] >= 0.0f && trans.state[0] <= Model.XMAX && trans.state[1] >= 0.0f&& trans.state[1] <= Model.YMAX)
			{
				trans.cost += Math.sqrt(2) / (trans.model.getTravelSpeed(trans.state[0], trans.state[1]));
				return trans;
			}
			else
				return null;
		}
		else
		{
			throw new RuntimeException("Shouldn't reach this point");
		}
	}
}
class CostComparator implements Comparator<GameState>
{
	public int compare(GameState a, GameState b)
	{
		if(a.cost < b.cost)
			return -1;
		else if(a.cost > b.cost)
			return 1;
		return 0;
	}
}
class StateComparator implements Comparator<GameState>
{
	public int compare(GameState a, GameState b)
	{
		for(int i = 0; i < 2; i++)
		{
			if(a.state[i] < b.state[i])
				return -1;
			else if(a.state[i] > b.state[i])
				return 1;
		}
		return 0;
	}
}