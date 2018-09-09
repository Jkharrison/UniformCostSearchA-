import java.util.ArrayList;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Color;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.LinkedList;
class Agent {
	MyPlanner plan = new MyPlanner();
	void drawPlan(Graphics g, Model m) {
		g.setColor(Color.red);
		g.drawLine((int)m.getX(), (int)m.getY(), (int)m.getDestinationX(), (int)m.getDestinationY());
	}

	void update(Model m)
	{
		Controller c = m.getController();
		while(true)
		{
			// System.out.println("Model X dest: " + m.getDestinationX());
			// System.out.println("Model Y dest: " + m.getDestinationY());
			MouseEvent e = c.nextMouseEvent();
			if(e == null)
				break;
			m.setDestination(e.getX(), e.getY());
			GameState init = new GameState(m);
			GameState goal = new GameState(m);
			GameState path = new GameState(m);
			path.state[0] = m.getX();
			path.state[1] = m.getY();
			goal.state[0] = m.getDestinationX();
			goal.state[1] = m.getDestinationY();
			path = goal;
			if(path.state[0] == goal.state[0] && path.state[1] == goal.state[1])
				throw new RuntimeException("This shouldn't happen unti later");
			while((int)path.state[0] != (int)goal.state[0] && (int)path.state[1] != (int)goal.state[1])
			{
				GameState current = plan.uniformCostSearch(init, path);
				LinkedList<GameState> results = GameState.getSuccessors(current);
				path = results.get(results.size()-2); // First step
				System.out.println("X: " + path.state[0]);
				System.out.println("Y: " + path.state[1]);
				m.setDestination(path.state[0], path.state[1]);
				init = path;
			}
		}
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
		this.state[0] = p.state[0];
		this.state[1] = p.state[1];
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
		return results;
	}
}
class MyPlanner
{
	GameState uniformCostSearch(GameState start, GameState goal)
	{
		CostComparator costComp = new CostComparator();
		StateComparator stateComp = new StateComparator();
		PriorityQueue<GameState> frontier = new PriorityQueue<GameState>(costComp);
		TreeSet<GameState> visited = new TreeSet<GameState>(stateComp);
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
					child.cost = s.cost + child.cost;
					child.parent = s;
					frontier.add(child);
					visited.add(child);
				}
			}
		}
		throw new RuntimeException("There is no such path to the goal");
	}
	GameState AStar(GameState start, GameState goal)
	{
		return null; // TODO: PlaceHolder for now.
	}
	GameState transition(GameState prev, int a)
	{
		GameState trans = new GameState(prev.cost, prev, prev.model);
		if(a == 0) // x+10, y-10
		{
			trans.cost += 1 / (trans.model.getTravelSpeed(prev.state[0] + 10.0f, prev.state[1] - 10f));
			trans.state[0] += 10;
			trans.state[1] -= 10;
			return trans;
		}
		else if(a == 1) // x+10, y
		{
			trans.cost += 1 / (trans.model.getTravelSpeed(prev.state[0] + 10.0f, prev.state[1]));
			trans.state[0] += 10;
			return trans;
		}
		else if(a == 2) // x+10, y+10
		{
			trans.cost += 1 / (trans.model.getTravelSpeed(prev.state[0] + 10.0f, prev.state[1] + 10.0f));
			trans.state[0] += 10;
			trans.state[1] += 10;
			return trans;
		}
		else if(a == 3) // x, y+10
		{
			trans.cost += 1 / (trans.model.getTravelSpeed(prev.state[0], prev.state[1] + 10.0f));
			trans.state[1] += 10;
			return trans;
		}
		else if(a == 4) // x, y-10
		{
			trans.cost += 1 / (trans.model.getTravelSpeed(prev.state[0], prev.state[1] - 10.0f));
			trans.state[1] -= 10;
			return trans;
		}
		else if(a == 5)// x-10, y-10
		{
			trans.cost += 1 / (trans.model.getTravelSpeed(prev.state[0] - 10.0f, prev.state[1] - 10.0f));
			trans.state[0] -= 10;
			trans.state[1] -= 10;
			return trans;
		}
		else if(a == 6) // x-10, y
		{
			trans.cost += 1 / (trans.model.getTravelSpeed(prev.state[0] - 10.0f, prev.state[1]));
			trans.state[0] -= 10;
			return trans;
		}
		else if(a == 7) // x-10, y+10
		{
			trans.cost += 1 / (trans.model.getTravelSpeed(prev.state[0] - 10.0f, prev.state[1] + 10.0f));
			trans.state[0] -= 10;
			trans.state[1] += 10;
			return trans;
		}
		else
		{
			throw new RuntimeException("Shouldn't reach this point");
		}
		//return null; // TODO: PlaceHolder for now.
	}
	float actionCost(GameState prev, int a)
	{
		return 0.0f; // TODO: PlaceHolder for now.
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
